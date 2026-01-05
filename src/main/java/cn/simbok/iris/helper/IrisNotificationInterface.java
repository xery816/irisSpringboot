package cn.simbok.iris.helper;

public interface IrisNotificationInterface {
    int IDX_NONE = 0;
    int IDX_CAPTURE = 1;
    int IDX_STRANGER = 2;
    int IDX_TOONEAR = 3;
    int IDX_TOOFAR = 4;
    int IDX_ZSSBJ = 5;
    int IDX_RETRY = 6;
    int IDX_ENROLL = 7;
    int IDX_VERIFY = 8;
    int IDX_IDENTIFY = 9;
    int IDX_UPWARD = 10;
    int IDX_DOWNWARD = 11;
    int IDX_LEFT = 12;
    int IDX_RIGHT = 13;
    int IDX_ENROLL_FAIL = 17;
    int IDX_VERIFY_FAIL = 18;
    int IDX_IDENTIFY_FAIL = 19;

    int IDX_NEEDFINGERPRINT = 0x0f14;
    int IDX_SBBPP = 0x0f15;
    int IDX_IRISPASSED = 0x0f16;
    int IDX_FACEPASSED = 0x0f17;
    int IDX_FINGERPASSED = 0x0f18;

    int IDX_MBZCCG = 0x0f19;
    int IDX_MBZCSB = 0x0f20;
    int IDX_ZWZCCG = 0x0f21;
    int IDX_ZWZCSB = 0x0f22;

    int IDX_TRUMPET = 0x0f23;
    int IDX_CARDPASS = 0x0f24;

    int play(int i);
}

