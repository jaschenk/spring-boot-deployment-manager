package jeffaschenk.infra.sbdm.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {
    private static ThreadLocal<RequestInfo> threadLocalRequest = new ThreadLocal<>();

    /**
     * Set ThreadLocal Request Info Object from incoming Request.
     * @param request Incoming HttpServletRequest
     */
    public static void setRequestInfo(HttpServletRequest request) {
        RequestInfo requestInfo =  threadLocalRequest.get();
        if (requestInfo == null) {
            requestInfo = new RequestInfo(request);
            threadLocalRequest.set(requestInfo);
        }
    }

    /**
     * Get ThreadLocal Request Info Object, set from original incoming request.
     * @return RequestInfo Wrapper Pojo for Original Request Wrapper.
     */
    public static RequestInfo getRequestInfo() {
        return threadLocalRequest.get();
    }

    /**
     * Clear the existing ThreadLocal Request Info Object.
     *
     * This method, must be called prior to sending any response, to clear out prior
     * information.  As this Thread will be re-used once placed back in the pool.
     */
    public static void clearRequestInfo() {
        threadLocalRequest.set(null);
    }
}
