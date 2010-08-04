import otherpackage.OtherPackagePublicEnum;

public class Main {
    /** used by {@link #basisCall} */
    static private int basisTestValue = 12;

    static public void main(String[] args) throws Exception {
        boolean timing = (args.length >= 1) && args[0].equals("--timing");
        run(timing);
    }

    static public void run(boolean timing) {
        preTest();

        long time0 = System.nanoTime();
        int count1 = test1(500);
        long time1 = System.nanoTime();
        int count2 = test2(500);
        long time2 = System.nanoTime();
        int count3 = test3(500);
        long time3 = System.nanoTime();
        int count4 = basis(2000);
        long time4 = System.nanoTime();

        System.out.println("basis: performed " + count4 + " iterations");
        System.out.println("test1: performed " + count1 + " iterations");
        System.out.println("test2: performed " + count2 + " iterations");
        System.out.println("test3: performed " + count3 + " iterations");

        double msec1 = (time1 - time0) / (double) count1 / 1000000;
        double msec2 = (time2 - time1) / (double) count2 / 1000000;
        double msec3 = (time3 - time2) / (double) count3 / 1000000;
        double basisMsec = (time4 - time3) / (double) count4 / 1000000;

        double avg = (msec1 + msec2 + msec3) / 3;
        if (avg < (basisMsec * 25)) {
            System.out.println("Timing is acceptable.");
        } else {
            System.out.println("Iterations are taking too long!");
            timing = true;
        }

        if (timing) {
            System.out.printf("basis time: %.3g msec\n", basisMsec);
            System.out.printf("test1: %.3g msec per iteration\n", msec1);
            System.out.printf("test2: %.3g msec per iteration\n", msec2);
            System.out.printf("test3: %.3g msec per iteration\n", msec3);
        }

    }

    static public void preTest() {
        /*
         * This is meant to ensure that the basic enum functionality
         * really is working.
         */

        Class<SamePackagePublicEnum> c = SamePackagePublicEnum.class;

        System.out.println(Enum.valueOf(c, "FOUR"));
        System.out.println(Enum.valueOf(c, "ONE"));
        System.out.println(Enum.valueOf(c, "FOURTEEN"));
        System.out.println(Enum.valueOf(c, "NINE"));
        System.out.println(Enum.valueOf(c, "FIVE"));
        System.out.println(Enum.valueOf(c, "TWELVE"));

        System.out.println(Enum.valueOf(c, "ZERO").getClass().getName());
    }

    static public int basis(int iters) {
        /*
         * The basis time is the time taken to call a static method
         * passing two arguments, which in turn accesses a static
         * variable, compares a string, and does a little trivial math
         * and a trivial comparison. (That is, this is a mini
         * "omnibus" performance metric.) This is clearly going to be
         * much faster than Enum.valueOf(), which is why we multiply
         * the time before testing.
         */
        for (int i = iters; i > 0; i--) {
            basisCall(i, "aname");
            basisCall(i, "bname");
            basisCall(i, "cname");
            basisCall(i, "dname");
            basisCall(i, "ename");
            basisCall(i, "fname");
            basisCall(i, "gname");
            basisCall(i, "hname");
            basisCall(i, "iname");
            basisCall(i, "jname");
            basisCall(i, "kname");
            basisCall(i, "lname");
            basisCall(i, "mname");
            basisCall(i, "nname");
            basisCall(i, "oname");
            basisCall(i, "pname");
            basisCall(i, "qname");
            basisCall(i, "rname");
            basisCall(i, "sname");
            basisCall(i, "tname");
        }

        return iters * 20;
    }

    static public int basisCall(int i, String name) {
        int compare = name.compareTo("fuzzbot");

        if (i < (basisTestValue * compare)) {
            return basisTestValue;
        } else {
            return i;
        }
    }

    static public int test1(int iters) {
        Class<SamePackagePublicEnum> c = SamePackagePublicEnum.class;
        for (int i = iters; i > 0; i--) {
            Enum.valueOf(c, "ZERO");
            Enum.valueOf(c, "ONE");
            Enum.valueOf(c, "TWO");
            Enum.valueOf(c, "THREE");
            Enum.valueOf(c, "FOUR");
            Enum.valueOf(c, "FIVE");
            Enum.valueOf(c, "SIX");
            Enum.valueOf(c, "SEVEN");
            Enum.valueOf(c, "EIGHT");
            Enum.valueOf(c, "NINE");
            Enum.valueOf(c, "TEN");
            Enum.valueOf(c, "ELEVEN");
            Enum.valueOf(c, "TWELVE");
            Enum.valueOf(c, "THIRTEEN");
            Enum.valueOf(c, "FOURTEEN");
            Enum.valueOf(c, "FIFTEEN");
            Enum.valueOf(c, "SIXTEEN");
            Enum.valueOf(c, "SEVENTEEN");
            Enum.valueOf(c, "EIGHTEEN");
            Enum.valueOf(c, "NINETEEN");
        }

        return iters * 20;
    }

    static public int test2(int iters) {
        Class<SamePackagePrivateEnum> c = SamePackagePrivateEnum.class;
        for (int i = iters; i > 0; i--) {
            Enum.valueOf(c, "ZERO");
            Enum.valueOf(c, "ONE");
            Enum.valueOf(c, "TWO");
            Enum.valueOf(c, "THREE");
            Enum.valueOf(c, "FOUR");
            Enum.valueOf(c, "FIVE");
            Enum.valueOf(c, "SIX");
            Enum.valueOf(c, "SEVEN");
            Enum.valueOf(c, "EIGHT");
            Enum.valueOf(c, "NINE");
            Enum.valueOf(c, "TEN");
            Enum.valueOf(c, "ELEVEN");
            Enum.valueOf(c, "TWELVE");
            Enum.valueOf(c, "THIRTEEN");
            Enum.valueOf(c, "FOURTEEN");
            Enum.valueOf(c, "FIFTEEN");
            Enum.valueOf(c, "SIXTEEN");
            Enum.valueOf(c, "SEVENTEEN");
            Enum.valueOf(c, "EIGHTEEN");
            Enum.valueOf(c, "NINETEEN");
        }

        return iters * 20;
    }

    static public int test3(int iters) {
        Class<OtherPackagePublicEnum> c = OtherPackagePublicEnum.class;
        for (int i = iters; i > 0; i--) {
            Enum.valueOf(c, "ZERO");
            Enum.valueOf(c, "ONE");
            Enum.valueOf(c, "TWO");
            Enum.valueOf(c, "THREE");
            Enum.valueOf(c, "FOUR");
            Enum.valueOf(c, "FIVE");
            Enum.valueOf(c, "SIX");
            Enum.valueOf(c, "SEVEN");
            Enum.valueOf(c, "EIGHT");
            Enum.valueOf(c, "NINE");
            Enum.valueOf(c, "TEN");
            Enum.valueOf(c, "ELEVEN");
            Enum.valueOf(c, "TWELVE");
            Enum.valueOf(c, "THIRTEEN");
            Enum.valueOf(c, "FOURTEEN");
            Enum.valueOf(c, "FIFTEEN");
            Enum.valueOf(c, "SIXTEEN");
            Enum.valueOf(c, "SEVENTEEN");
            Enum.valueOf(c, "EIGHTEEN");
            Enum.valueOf(c, "NINETEEN");
        }

        return iters * 20;
    }
}
