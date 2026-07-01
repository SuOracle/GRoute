package gozarcore

import (
	"context"
	"fmt"
	"io"
	"net"
	"net/http"
	"os"
	"strconv"
	"strings"
	"sync"
	"sync/atomic"
	"time"

	xlog "github.com/xtls/xray-core/common/log"
	v2net "github.com/xtls/xray-core/common/net"
	"github.com/xtls/xray-core/core"
	"github.com/xtls/xray-core/features/stats"
	"github.com/xtls/xray-core/infra/conf/serial"
	_ "github.com/xtls/xray-core/main/distro/all"
)

var (
	mu       sync.Mutex
	instance *core.Instance
	tunFd    int = -1
)

type Logger interface {
	Log(line string)
}

var logger Logger

type logForwarder struct{}

func (logForwarder) Handle(msg xlog.Message) {
	if l := logger; l != nil {
		l.Log(msg.String())
	}
}

func SetLogger(l Logger) {
	logger = l
	xlog.RegisterHandler(logForwarder{})
}

func XrayVersion() string {
	return core.Version()
}

func Start(configJSON string, fd int) error {
	mu.Lock()
	defer mu.Unlock()
	if instance != nil {
		instance.Close()
		instance = nil
	}
	if tunFd >= 0 {
		if f := os.NewFile(uintptr(tunFd), "tun"); f != nil {
			f.Close()
		}
		tunFd = -1
	}
	tunFd = fd
	os.Setenv("XRAY_TUN_FD", strconv.Itoa(fd))
	config, err := serial.LoadJSONConfig(strings.NewReader(configJSON))
	if err != nil {
		return err
	}
	inst, err := core.New(config)
	if err != nil {
		return err
	}
	if err := inst.Start(); err != nil {
		inst.Close()
		return err
	}
	instance = inst
	return nil
}

func Stop() error {
	mu.Lock()
	defer mu.Unlock()
	if tunFd >= 0 {
		if f := os.NewFile(uintptr(tunFd), "tun"); f != nil {
			f.Close()
		}
		tunFd = -1
	}
	var err error
	if instance != nil {
		err = instance.Close()
		instance = nil
	}
	return err
}

func IsRunning() bool {
	mu.Lock()
	defer mu.Unlock()
	return instance != nil
}

func SetAssetPath(path string) {
	os.Setenv("XRAY_LOCATION_ASSET", path)
}

func readCounter(name string) int64 {
	mu.Lock()
	defer mu.Unlock()
	if instance == nil {
		return 0
	}
	feature := instance.GetFeature(stats.ManagerType())
	if feature == nil {
		return 0
	}
	manager, ok := feature.(stats.Manager)
	if !ok {
		return 0
	}
	counter := manager.GetCounter(name)
	if counter == nil {
		return 0
	}
	return counter.Value()
}

func QueryUplink() int64 {
	return readCounter("outbound>>>proxy>>>traffic>>>uplink")
}

func QueryDownlink() int64 {
	return readCounter("outbound>>>proxy>>>traffic>>>downlink")
}

func MeasureDelay(configJSON string) int64 {
	config, err := serial.LoadJSONConfig(strings.NewReader(configJSON))
	if err != nil {
		return -1
	}
	inst, err := core.New(config)
	if err != nil {
		return -1
	}
	if err := inst.Start(); err != nil {
		return -1
	}
	defer inst.Close()

	transport := &http.Transport{
		TLSHandshakeTimeout: 6 * time.Second,
		DialContext: func(ctx context.Context, network, addr string) (net.Conn, error) {
			dest, err := v2net.ParseDestination(network + ":" + addr)
			if err != nil {
				return nil, err
			}
			return core.Dial(ctx, inst, dest)
		},
	}
	client := &http.Client{Transport: transport, Timeout: 10 * time.Second}
	defer transport.CloseIdleConnections()

	const url = "https://www.gstatic.com/generate_204"

	warm, err := client.Get(url)
	if err != nil {
		return -1
	}
	io.Copy(io.Discard, warm.Body)
	warm.Body.Close()

	start := time.Now()
	resp, err := client.Get(url)
	if err != nil {
		return -1
	}
	io.Copy(io.Discard, resp.Body)
	resp.Body.Close()
	return time.Since(start).Milliseconds()
}

var (
	stMu    sync.Mutex
	stPhase string
	stLive  float64
)

func stSet(phase string, live float64) {
	stMu.Lock()
	stPhase = phase
	stLive = live
	stMu.Unlock()
}

func stLiveSet(live float64) {
	stMu.Lock()
	stLive = live
	stMu.Unlock()
}

func SpeedTestPhase() string {
	stMu.Lock()
	defer stMu.Unlock()
	return stPhase
}

func SpeedTestLive() float64 {
	stMu.Lock()
	defer stMu.Unlock()
	return stLive
}

func stClient(inst *core.Instance) *http.Client {
	transport := &http.Transport{
		DisableKeepAlives:   false,
		MaxIdleConns:        64,
		MaxIdleConnsPerHost: 16,
		TLSHandshakeTimeout: 8 * time.Second,
		DialContext: func(ctx context.Context, network, addr string) (net.Conn, error) {
			dest, err := v2net.ParseDestination(network + ":" + addr)
			if err != nil {
				return nil, err
			}
			return core.Dial(ctx, inst, dest)
		},
	}
	return &http.Client{Transport: transport, Timeout: 35 * time.Second}
}

func stPingOnce(client *http.Client) float64 {
	req, err := http.NewRequest("GET", "https://www.gstatic.com/generate_204", nil)
	if err != nil {
		return -1
	}
	start := time.Now()
	resp, err := client.Do(req)
	if err != nil {
		return -1
	}
	io.Copy(io.Discard, resp.Body)
	resp.Body.Close()
	return float64(time.Since(start).Milliseconds())
}

func stPingPhase(client *http.Client) (float64, float64) {
	stPingOnce(client)
	samples := make([]float64, 0, 10)
	for i := 0; i < 10; i++ {
		ms := stPingOnce(client)
		if ms >= 0 {
			samples = append(samples, ms)
			stLiveSet(ms)
		}
		time.Sleep(100 * time.Millisecond)
	}
	if len(samples) == 0 {
		return 0, 0
	}
	var sum float64
	for _, v := range samples {
		sum += v
	}
	avg := sum / float64(len(samples))
	jit := 0.0
	if len(samples) == 2 {
		d := samples[1] - samples[0]
		if d < 0 {
			d = -d
		}
		jit = d
	} else if len(samples) > 2 {
		var jsum, jmax float64
		cnt := 0
		for i := 1; i < len(samples); i++ {
			d := samples[i] - samples[i-1]
			if d < 0 {
				d = -d
			}
			jsum += d
			if d > jmax {
				jmax = d
			}
			cnt++
		}
		jit = (jsum - jmax) / float64(cnt-1)
	}
	return avg, jit
}

func stPeakTicker(total *int64, stop <-chan struct{}, peak *float64) {
	last := atomic.LoadInt64(total)
	lastT := time.Now()
	startT := time.Now()
	for {
		select {
		case <-stop:
			return
		case <-time.After(500 * time.Millisecond):
			cur := atomic.LoadInt64(total)
			d := time.Since(lastT).Seconds()
			win := cur - last
			last = cur
			lastT = time.Now()
			if d <= 0 {
				continue
			}
			rate := float64(win) * 8.0 / 1e6 / d
			stLiveSet(rate)
			if time.Since(startT) > 1200*time.Millisecond && rate > *peak {
				*peak = rate
			}
		}
	}
}

func stCumTicker(total *int64, measuredNs *int64, stop <-chan struct{}) {
	for {
		select {
		case <-stop:
			return
		case <-time.After(300 * time.Millisecond):
			ns := atomic.LoadInt64(measuredNs)
			if ns == 0 {
				continue
			}
			cur := atomic.LoadInt64(total)
			secs := time.Since(time.Unix(0, ns)).Seconds()
			if secs > 0 {
				stLiveSet(float64(cur) * 8.0 / 1e6 / secs)
			}
		}
	}
}

func stDownload(client *http.Client) float64 {
	const phaseMs = int64(8000)
	const streams = 4
	var total int64
	var measuredNs int64
	start := time.Now()
	var wg sync.WaitGroup
	for i := 0; i < streams; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			buf := make([]byte, 64*1024)
			for time.Since(start).Milliseconds() < phaseMs {
				req, err := http.NewRequest("GET", "https://speed.cloudflare.com/__down?bytes=26214400", nil)
				if err != nil {
					return
				}
				resp, err := client.Do(req)
				if err != nil {
					time.Sleep(150 * time.Millisecond)
					continue
				}
				for {
					n, e := resp.Body.Read(buf)
					if n > 0 {
						atomic.CompareAndSwapInt64(&measuredNs, 0, time.Now().UnixNano())
						atomic.AddInt64(&total, int64(n))
					}
					if e != nil {
						break
					}
					if time.Since(start).Milliseconds() > phaseMs {
						break
					}
				}
				resp.Body.Close()
			}
		}()
	}
	stop := make(chan struct{})
	done := make(chan struct{})
	var peak float64
	go func() {
		stPeakTicker(&total, stop, &peak)
		close(done)
	}()
	wg.Wait()
	close(stop)
	<-done

	if peak > 0 {
		return peak
	}
	ns := atomic.LoadInt64(&measuredNs)
	tot := atomic.LoadInt64(&total)
	if ns == 0 || tot == 0 {
		return 0
	}
	secs := time.Since(time.Unix(0, ns)).Seconds()
	if secs <= 0 {
		return 0
	}
	return float64(tot) * 8.0 / 1e6 / secs
}

type stUploadReader struct {
	remaining int64
}

func (r *stUploadReader) Read(p []byte) (int, error) {
	if r.remaining <= 0 {
		return 0, io.EOF
	}
	n := len(p)
	if int64(n) > r.remaining {
		n = int(r.remaining)
	}
	r.remaining -= int64(n)
	return n, nil
}

func stUpload(client *http.Client) float64 {
	const phaseMs = int64(8000)
	const streams = 3
	const chunk = int64(2 * 1024 * 1024)
	var total int64
	var measuredNs int64
	start := time.Now()
	var wg sync.WaitGroup
	for i := 0; i < streams; i++ {
		wg.Add(1)
		go func() {
			defer wg.Done()
			for time.Since(start).Milliseconds() < phaseMs {
				body := &stUploadReader{remaining: chunk}
				req, err := http.NewRequest("POST", "https://speed.cloudflare.com/__up", body)
				if err != nil {
					return
				}
				req.ContentLength = chunk
				req.Header.Set("Content-Type", "application/octet-stream")
				atomic.CompareAndSwapInt64(&measuredNs, 0, time.Now().UnixNano())
				resp, err := client.Do(req)
				if err != nil {
					time.Sleep(150 * time.Millisecond)
					continue
				}
				io.Copy(io.Discard, resp.Body)
				resp.Body.Close()
				atomic.AddInt64(&total, chunk)
			}
		}()
	}
	stop := make(chan struct{})
	go stCumTicker(&total, &measuredNs, stop)
	wg.Wait()
	close(stop)

	ns := atomic.LoadInt64(&measuredNs)
	tot := atomic.LoadInt64(&total)
	if ns == 0 || tot == 0 {
		return 0
	}
	secs := time.Since(time.Unix(0, ns)).Seconds()
	if secs <= 0 {
		return 0
	}
	return float64(tot) * 8.0 / 1e6 / secs
}

func stLoadedLatency(client *http.Client, stop <-chan struct{}, out *float64) {
	samples := make([]float64, 0, 32)
	for {
		select {
		case <-stop:
			if len(samples) > 0 {
				var s float64
				for _, v := range samples {
					s += v
				}
				*out = s / float64(len(samples))
			}
			return
		default:
		}
		ms := stPingOnce(client)
		if ms >= 0 {
			samples = append(samples, ms)
		}
		time.Sleep(250 * time.Millisecond)
	}
}

func RunSpeedTest(configJSON string) string {
	stSet("ping", 0)
	config, err := serial.LoadJSONConfig(strings.NewReader(configJSON))
	if err != nil {
		stSet("error", 0)
		return `{"error":"config"}`
	}
	inst, err := core.New(config)
	if err != nil {
		stSet("error", 0)
		return `{"error":"core"}`
	}
	if err := inst.Start(); err != nil {
		stSet("error", 0)
		return `{"error":"start"}`
	}
	defer inst.Close()

	client := stClient(inst)
	latClient := stClient(inst)

	stSet("ping", 0)
	idle, jit := stPingPhase(client)

	stSet("download", 0)
	stopDl := make(chan struct{})
	var dlLat float64
	var wgDl sync.WaitGroup
	wgDl.Add(1)
	go func() {
		defer wgDl.Done()
		stLoadedLatency(latClient, stopDl, &dlLat)
	}()
	dl := stDownload(client)
	close(stopDl)
	wgDl.Wait()

	stSet("upload", 0)
	stopUl := make(chan struct{})
	var ulLat float64
	var wgUl sync.WaitGroup
	wgUl.Add(1)
	go func() {
		defer wgUl.Done()
		stLoadedLatency(latClient, stopUl, &ulLat)
	}()
	ul := stUpload(client)
	close(stopUl)
	wgUl.Wait()

	stSet("done", 0)
	return fmt.Sprintf(
		`{"download":%.2f,"upload":%.2f,"idle":%.2f,"jitter":%.2f,"dlLatency":%.2f,"ulLatency":%.2f}`,
		dl, ul, idle, jit, dlLat, ulLat)
}