import com.android.build.gradle.api.LibraryVariant

plugins {
    alias(libs.plugins.kotlin.android)
    id("com.android.library")
}

android {
    buildFeatures {
        aidl = true
        buildConfig = true
    }
    //TODO Change to com.objmobile
    namespace = "de.blinkt.openvpn"
    compileSdk = 36
    //compileSdkPreview = "UpsideDownCake"

    // Also update runcoverity.sh
    ndkVersion = "28.0.13004108"

    defaultConfig {
        minSdk = 21
        externalNativeBuild {
            cmake {
                //arguments+= "-DCMAKE_VERBOSE_MAKEFILE=1"
                arguments += listOf(
                    "-DANDROID_STL=c++_shared", "-DANDROID_SUPPORT_FLEXIBLE_PAGE_SIZES=ON"
                )
                cppFlags += listOf("-std=c++17", "-frtti", "-fexceptions")
                abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
            }
        }
        ndk {
            abiFilters += setOf("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }


    //testOptions.unitTests.isIncludeAndroidResources = true

    externalNativeBuild {
        cmake {
            path = File("${projectDir}/src/main/cpp/CMakeLists.txt")
        }
    }

    lint {
        enable += setOf("BackButton", "EasterEgg", "StopShip", "IconExpectedSize", "GradleDynamicVersion", "NewerVersionAvailable")
        checkOnly += setOf("ImpliedQuantity", "MissingQuantity")
        disable += setOf("MissingTranslation", "UnsafeNativeCodeLocation")
    }


    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
            keepDebugSymbols += "**/*.so"
            pickFirsts += "**/libc++_shared.so"
            pickFirsts += "**/libovpnexec.so"
            pickFirsts += "**/libopenvpn.so"
            pickFirsts += "**/libosslspeedtest.so"
            pickFirsts += "**/libosslutil.so"
            pickFirsts += "**/libovpn3.so"
            pickFirsts += "**/libovpnutil.so"
        }
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    sourceSets {
        getByName("main") { // Include the native executables as JNI libs
            jniLibs.srcDirs("src/main/jniLibs")
        }
    }

}

var swigcmd = "swig"
// Workaround for macOS(arm64) and macOS(intel) since it otherwise does not find swig and
// I cannot get the Exec task to respect the PATH environment :(
if (file("/opt/homebrew/bin/swig").exists())
    swigcmd = "/opt/homebrew/bin/swig"
else if (file("/usr/local/bin/swig").exists())
    swigcmd = "/usr/local/bin/swig"
if (file("C:\\swig\\swig.exe").exists())
    swigcmd = "C:\\swig\\swig.exe"


fun registerGenTask(variantName: String, variantDirName: String): File {
    val baseDir = File(buildDir, "generated/source/ovpn3swig/${variantDirName}")
    val genDir = File(baseDir, "net/openvpn/ovpn3")

    tasks.register<Exec>("generateOpenVPN3Swig${variantName}")
    {

        doFirst {
            mkdir(genDir)
        }
        commandLine(listOf(swigcmd, "-outdir", genDir, "-outcurrentdir", "-c++", "-java", "-package", "net.openvpn.ovpn3",
                "-Isrc/main/cpp/openvpn3/client", "-Isrc/main/cpp/openvpn3/",
                "-DOPENVPN_PLATFORM_ANDROID",
                "-o", "${genDir}/ovpncli_wrap.cxx", "-oh", "${genDir}/ovpncli_wrap.h",
                "src/main/cpp/openvpn3/client/ovpncli.i"))
        inputs.files( "src/main/cpp/openvpn3/client/ovpncli.i")
        outputs.dir( genDir)

    }
    return baseDir
}

android.libraryVariants.all(object : Action<LibraryVariant> {
    override fun execute(variant: LibraryVariant) {
        val sourceDir = registerGenTask(variant.name, variant.baseName.replace("-", "/"))
        val task = tasks.named("generateOpenVPN3Swig${variant.name}").get()

        variant.registerJavaGeneratingTask(task, sourceDir)
    }
}) // Ensure all native libraries are included in the AAR
tasks.register<Copy>("copyNativeExecutables") { // Don't depend on build tasks to avoid circular dependency
    from(layout.buildDirectory.dir("intermediates/cxx")) { // Include all native libraries and executables from CXX build output
        include("**/obj/**/libovpnexec.so")
        include("**/obj/**/libopenvpn.so")
        include("**/obj/**/libosslspeedtest.so")
        include("**/obj/**/libosslutil.so")
        include("**/obj/**/libovpn3.so")
        include("**/obj/**/libovpnutil.so")
        include("**/obj/**/pie_openvpn.*") // Include any other .so files that might be generated
        include("**/obj/**/*.so")
    }
    into(layout.projectDirectory.dir("src/main/jniLibs")) // Handle duplicates by keeping the first one found
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    eachFile { // Extract ABI from path like: Debug/4h5s4u5e/obj/arm64-v8a/libovpnexec.so
        val abiMatch = Regex(".*/obj/([^/]+)/.*").find(path)
        if (abiMatch != null) {
            val abi = abiMatch.groupValues[1]
            path = "$abi/${name}"
        }
    }
    includeEmptyDirs = false

    doFirst {
        println("Copying native libraries from CXX build output to jniLibs...") // Clean existing jniLibs to avoid stale files
        val jniLibsDir = layout.projectDirectory.dir("src/main/jniLibs").asFile
        if (jniLibsDir.exists()) {
            jniLibsDir.deleteRecursively()
        }
        jniLibsDir.mkdirs()
    }

    doLast {
        println("Native libraries copied successfully")
    }
} // Hook the copy task to run after native build completes
tasks.whenTaskAdded {
    if (name.startsWith("externalNativeBuild")) {
        finalizedBy("copyNativeExecutables")
    }
} // Task to verify native libraries are present
tasks.register("verifyNativeLibs") {
    dependsOn("copyNativeExecutables")

    doLast {
        val jniLibsDir = layout.projectDirectory.dir("src/main/jniLibs")
        val expectedLibs = listOf(
            "libovpnexec.so",
            "libopenvpn.so",
            "libosslspeedtest.so",
            "libosslutil.so",
            "libovpn3.so",
            "libovpnutil.so"
        )
        val abis = listOf("arm64-v8a", "armeabi-v7a", "x86", "x86_64")

        println("Verifying native libraries...")

        abis.forEach { abi ->
            val abiDir = jniLibsDir.dir(abi)
            if (abiDir.asFile.exists()) {
                println("ABI: $abi")
                expectedLibs.forEach { lib ->
                    val libFile = abiDir.file(lib)
                    if (libFile.asFile.exists()) {
                        println("  ✓ $lib (${libFile.asFile.length()} bytes)")
                    } else {
                        println("  ✗ $lib (missing)")
                    }
                }
            } else {
                println("ABI directory missing: $abi")
            }
        }
    }
}

dependencies {
    // https://maven.google.com/web/index.html
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core.ktx)

    implementation(libs.android.view.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.cardview)
    implementation(libs.androidx.viewpager2)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.preference.ktx)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.security.crypto)
    implementation(libs.androidx.webkit)
    implementation(libs.kotlin)
    implementation(libs.mpandroidchart)
    implementation(libs.square.okhttp)
    implementation(project(":vpn:domain"))
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("com.google.code.gson:gson:2.11.0")
}