package eu.urbreathdsjobs.backoffice;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "backoffice.security")
public class BackofficeSecurityProperties {

    private String token;
    private boolean allowQueryParam = true;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isAllowQueryParam() {
        return allowQueryParam;
    }

    public void setAllowQueryParam(boolean allowQueryParam) {
        this.allowQueryParam = allowQueryParam;
    }
}

