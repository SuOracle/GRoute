plugins {
	id("com.android.library")
}

android {
	namespace = "org.strongswan.android"
	compileSdk {
		version = release(36) {
			minorApiLevel = 1
		}
	}

	ndkVersion = "27.3.13750724"

	defaultConfig {
		minSdk = 26

		externalNativeBuild {
			ndkBuild {
				arguments += "-j" + Runtime.getRuntime().availableProcessors()
				arguments += "APP_ALLOW_MISSING_DEPS=true"
				cFlags += "-DHAVE_SIGWAITINFO"
			}
		}

		ndk {
			abiFilters += listOf("arm64-v8a", "armeabi-v7a")
		}

		consumerProguardFiles("consumer-rules.pro")

	}

	externalNativeBuild {
		ndkBuild {
			path = file("src/frontends/android/app/src/main/jni/Android.mk")
		}
	}

	sourceSets {
		getByName("main") {
			manifest.srcFile("src/main/AndroidManifest.xml")
			java.directories.clear()
			java.directories.add("src/upstream-java")
			java.directories.add("src/patched")
			res.directories.clear()
			res.directories.add("src/frontends/android/app/src/main/res")
		}
	}

	buildTypes {
		release {
			isMinifyEnabled = false
		}
	}

	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}

	packaging {
		jniLibs {
			useLegacyPackaging = true
		}
	}

	lint {
		checkReleaseBuilds = false
		abortOnError = false
		disable += "all"
	}

	buildFeatures {
		buildConfig = false
	}
}

val syncUpstreamJava by tasks.registering(Sync::class) {
	from("src/frontends/android/app/src/main/java") {
		exclude("org/strongswan/android/ui/**")
	}
	into(layout.projectDirectory.dir("src/upstream-java"))
}

tasks.named("preBuild") {
	dependsOn(syncUpstreamJava)
}

dependencies {
	implementation("androidx.appcompat:appcompat:1.7.0")
	implementation("androidx.localbroadcastmanager:localbroadcastmanager:1.1.0")
	implementation("androidx.preference:preference:1.2.1")
	implementation("com.google.android.material:material:1.12.0")
}
