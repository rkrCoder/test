package com.crimson.picshu.gateway;

public enum AppEnvironment {

    PRODUCTION {
        @Override
        public String merchant_Key() {
            return "qqCO5jJZ";
        }

        @Override
        public String merchant_ID() {
            return "6019286";
        }

        @Override
        public String furl() {
            return "http://www.lavyainteriors.com/fail.php";
        }
//"https://www.payumoney.com/mobileapp/payumoney/success.php"
        @Override
        public String surl() {
            return "http://www.lavyainteriors.com/success.php";
        }

        @Override
        public String salt() {
            return "6wgyRKLiQM";
        }

        @Override
        public boolean debug() {
            return false;
        }
    };

    public abstract String merchant_Key();

    public abstract String merchant_ID();

    public abstract String furl();

    public abstract String surl();

    public abstract String salt();

    public abstract boolean debug();

}
