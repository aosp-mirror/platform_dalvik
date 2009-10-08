// Copyright 2009 Google Inc. All Rights Reserved.

import java.util.UUID;

/**
 * Copy the current Android sourcecode into Apache Harmony, where it can be
 * reviewed and submitted to their SVN. Only run this script after first merging
 * the latest harmony code into Android.
 */
public class PushAndroidCode {

    private final String androidPath;
    private final String harmonyPath;

    public PushAndroidCode(String androidPath, String harmonyPath) {
        this.androidPath = androidPath;
        this.harmonyPath = harmonyPath;
    }

    public void push(Module module) {
        Filesystem filesystem = new Filesystem();

        // copy android code to a temp directory that is laid out like Harmony
        String temp = "/tmp/" + UUID.randomUUID();
        filesystem.mkdir(temp);
        filesystem.copyContents(androidPath + "/" + module.path(),
                temp + "/" + module.path());
        for (MappedDirectory mappedDirectory : module.getMappedDirectories()) {
            filesystem.moveContents(
                    temp + "/" + mappedDirectory.gitPath(),
                    temp + "/" + mappedDirectory.svnPath());
        }

        // clobber files from harmony with their Android equivalents
        filesystem.copyContents(temp + "/" + module.path(),
                harmonyPath + "/" + module.path());
    }

    public static void main(String[] args) {
        if (args.length < 3) {
            printUsage();
            return;
        }

        String androidPath = args[0] + "/dalvik/libcore";
        String harmonyPath = args[1] + "/working_classlib/modules";

        // TODO: validate directories?
        
        Module[] modules = new Module[args.length - 2];
        for (int i = 0; i < modules.length; i++) {
            modules[i] = Module.VALUES.get(args[i+2]);
            if (modules[i] == null) {
                System.out.println("No such module: " + args[i+2]);
                return;
            }
        }

        PushAndroidCode pusher = new PushAndroidCode(androidPath, harmonyPath);
        for (Module module : modules) {
            pusher.push(module);
        }
    }

    private static void printUsage() {
        System.out.println("This tool will clobber Harmony's core libraries with Android's copy");
        System.out.println("so that a patch can be submitted upstream.");
        System.out.println();
        System.out.println("Usage: PushAndroidCode <android_root> <harmony_root> <module>...");
        System.out.println();
        System.out.println("  <android_root> is the android git client directory that contains dalvik");
        System.out.println("                 This should hold an up-to-date checkout of Android. The");
        System.out.println("                 target modules should also be up-to-date with respect to");
        System.out.println("                 Harmony; use the PullHarmonyCode tool first if necessary.");
        System.out.println();
        System.out.println("  <harmony_root> is the android client directory that contains working_classlib.");
        System.out.println("                 This should hold an up-to-date checkout of Harmony.");
        System.out.println();
        System.out.println("  <module> is one of " + Module.VALUES.keySet());
        System.out.println();
        System.out.println("Example usage:");
        System.out.println("  java -cp out/host/linux-x86/framework/integrate.jar PushAndroidCode \\");
        System.out.println("    /usr/local/google/jesse/clients/jessewilson_g1 \\");
        System.out.println("    /usr/local/google/jesse/clients/jessewilson_h0/trunk \\");
        System.out.println("    crypto");
    }
}
