package jeffaschenk.infra.sbdm.util;

import javax.servlet.http.HttpServletRequest;
import java.util.UUID;

/**
 * Provides our Request Info wrapped within a POJO for Placing with a ThreadLocal Wrapper.
 */
public class RequestInfo {

    private String auditId;

    private Long requestTimeStamp;

    private HttpServletRequest request;

    public RequestInfo(Long requestTimeStamp, HttpServletRequest request) {
        this.auditId = UUID.randomUUID().toString();
        this.requestTimeStamp = requestTimeStamp;
        this.request = request;
    }

    public RequestInfo(HttpServletRequest request) {
        this.auditId = UUID.randomUUID().toString();
        this.requestTimeStamp = System.currentTimeMillis();
        this.request = request;
    }

    public Long getRequestTimeStamp() {
        return requestTimeStamp;
    }

    public void setRequestTimeStamp(Long requestTimeStamp) {
        this.requestTimeStamp = requestTimeStamp;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public String getAuditId() {
        return auditId;
    }
}

