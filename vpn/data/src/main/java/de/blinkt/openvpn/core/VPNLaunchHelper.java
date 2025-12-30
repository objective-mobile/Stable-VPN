/*
 * Copyright (c) 2012-2016 Arne Schwabe
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package de.blinkt.openvpn.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Vector;

import de.blinkt.openvpn.VpnProfile;

public class VPNLaunchHelper {
    private static final String MININONPIEVPN = "nopie_openvpn";
    private static final String MINIPIEVPN = "pie_openvpn";
    private static final String OVPNCONFIGFILE = "android.conf";


    private static String writeMiniVPN(Context context) {
        String nativeAPI = NativeUtils.getNativeAPI();
        File nativeLibDir = new File(context.getApplicationInfo().nativeLibraryDir);
        File ovpnExec = new File(nativeLibDir, "libovpnexec.so");

        Log.d("VPNLaunchHelper", "Looking for libovpnexec.so at: " + ovpnExec.getPath());
        Log.d("VPNLaunchHelper", "Looking for libovpnexec.so at: " + Arrays.toString(ovpnExec.list()));

        Log.d("VPNLaunchHelper", "Native lib dir exists: " + nativeLibDir.exists());
        Log.d("VPNLaunchHelper", "libovpnexec.so exists: " + ovpnExec.exists());

        if (ovpnExec.exists() && ovpnExec.canExecute()) {
            Log.d("VPNLaunchHelper", "Found executable libovpnexec.so at: " + ovpnExec.getPath());
            return ovpnExec.getPath();
        } else {
            throw new RuntimeException("libovpnexec.so not found in native library directory: " + nativeLibDir.getPath());
        }
//        /* Q does not allow executing binaries written in temp directory anymore */
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
//            // For Android P+, use libovpnexec.so from the native library directory
//            File nativeLibDir = new File(context.getApplicationInfo().nativeLibraryDir);
//            File ovpnExec = new File(nativeLibDir, "libovpnexec.so");
//
//            Log.d("VPNLaunchHelper", "Looking for libovpnexec.so at: " + ovpnExec.getPath());
//            Log.d("VPNLaunchHelper", "Looking for libovpnexec.so at: " + Arrays.toString(ovpnExec.list()));
//
//            Log.d("VPNLaunchHelper", "Native lib dir exists: " + nativeLibDir.exists());
//            Log.d("VPNLaunchHelper", "libovpnexec.so exists: " + ovpnExec.exists());
//
//            if (ovpnExec.exists() && ovpnExec.canExecute()) {
//                Log.d("VPNLaunchHelper", "Found executable libovpnexec.so at: " + ovpnExec.getPath());
//                return ovpnExec.getPath();
//            } else {
//                throw new RuntimeException("libovpnexec.so not found in native library directory: " + nativeLibDir.getPath());
//            }
//        }
//
//        // For older Android versions, extract from assets to cache directory
//        String[] abis;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
//            abis = getSupportedABIsLollipop();
//        else
//            //noinspection deprecation
//            abis = new String[]{Build.CPU_ABI, Build.CPU_ABI2};
//
//        if (!nativeAPI.equals(abis[0])) {
//            VpnStatus.logWarning(R.string.abi_mismatch, Arrays.toString(abis), nativeAPI);
//            abis = new String[]{nativeAPI};
//        }
//
//        for (String abi : abis) {
//            File vpnExecutable = new File(context.getCacheDir(), "c_" + getMiniVPNExecutableName() + "." + abi);
//            if ((vpnExecutable.exists() && vpnExecutable.canExecute()) || writeMiniVPNBinary(context, abi, vpnExecutable)) {
//                return vpnExecutable.getPath();
//            }
//        }
//
//        throw new RuntimeException("Cannot find any executable for this device's ABIs " + Arrays.toString(abis));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static String[] getSupportedABIsLollipop() {
        return Build.SUPPORTED_ABIS;
    }

    private static String getMiniVPNExecutableName() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            return MINIPIEVPN;
        else
            return MININONPIEVPN;
    }


    public static String[] replacePieWithNoPie(String[] mArgv) {
        mArgv[0] = mArgv[0].replace(MINIPIEVPN, MININONPIEVPN);
        return mArgv;
    }


    static String[] buildOpenvpnArgv(Context c) {
        Vector<String> args = new Vector<>();

        String binaryName = writeMiniVPN(c);
        Log.d("VPNLaunchHelper", "buildOpenvpnArgv: " + binaryName);
        Log.d("VPNLaunchHelper", "Native library dir: " + c.getApplicationInfo().nativeLibraryDir);
        Log.d("VPNLaunchHelper", "Cache dir: " + c.getCacheDir().getAbsolutePath());

        // Add fixed paramenters
        //args.add("/data/data/de.blinkt.openvpn/lib/openvpn");
        if (binaryName == null) {
            VpnStatus.logError("Error writing minivpn binary");
            return null;
        }

        args.add(binaryName);

        args.add("--config");
        args.add(getConfigFilePath(c));

        return args.toArray(new String[args.size()]);
    }

    private static boolean writeMiniVPNBinary(Context context, String abi, File mvpnout) {
        try {
            InputStream mvpn;

            try {
                mvpn = context.getAssets().open(getMiniVPNExecutableName() + "." + abi);
            } catch (IOException errabi) {
                VpnStatus.logInfo("Failed getting assets for archicture " + abi);
                return false;
            }


            FileOutputStream fout = new FileOutputStream(mvpnout);

            byte[] buf = new byte[4096];

            int lenread = mvpn.read(buf);
            while (lenread > 0) {
                fout.write(buf, 0, lenread);
                lenread = mvpn.read(buf);
            }
            fout.close();

            if (!mvpnout.setExecutable(true)) {
                VpnStatus.logError("Failed to make OpenVPN executable");
                return false;
            }


            return true;
        } catch (IOException e) {
            VpnStatus.logException(e);
            return false;
        }

    }


    public static void startOpenVpn(VpnProfile startprofile, Context context) {
        Intent startVPN = startprofile.prepareStartService(context);
        if (startVPN != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                //noinspection NewApi
                context.startForegroundService(startVPN);
            else
                context.startService(startVPN);

        }
    }


    public static String getConfigFilePath(Context context) {
        return context.getCacheDir().getAbsolutePath() + "/" + OVPNCONFIGFILE;
    }

}