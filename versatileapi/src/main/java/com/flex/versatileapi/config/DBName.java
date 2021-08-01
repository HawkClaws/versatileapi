package com.flex.versatileapi.config;

public enum DBName {
	DATA_STORE("DataStore"),
	API_SETTING_STORE("ApiSettingStore"),
	USER_STORE("UserStore"),
	AUTHENTICATION_GROUP_STORE("AuthenticationGroupStore"),
	;

    private final String text;

    private DBName(final String text) {
        this.text = text;
    }

    public String getString() {
        return this.text;
    }
}
