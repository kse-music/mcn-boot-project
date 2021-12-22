package cn.hiboot.mcn.autoconfigure.web.filter.xss;


import cn.hiboot.mcn.core.util.McnUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * prevent XSS attack
 *
 * @author DingHao
 * @since 2019/1/9 11:02
 */
public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private final boolean isIncludeRichText;

    public XssHttpServletRequestWrapper(HttpServletRequest request, boolean isIncludeRichText) {
        super(request);
        this.isIncludeRichText = isIncludeRichText;
    }

    /**
     * 覆盖getParameter方法，将参数名和参数值都做xss过滤。
     * 如果需要获得原始的值，则通过super.getParameterValues(name)来获取
     * getParameterNames,getParameterValues和getParameterMap也可能需要覆盖
     *
     * @param name header name
     * @return clean
     */
    @Override
    public String getParameter(String name) {
        boolean flag = "content".equals(name) || name.endsWith("WithHtml");
        if (flag && !isIncludeRichText) {
            return super.getParameter(name);
        }
        name = JsoupUtil.clean(name);
        String value = super.getParameter(name);
        if (McnUtils.isNotNullAndEmpty(value)) {
            value = JsoupUtil.clean(value);
        }
        return value;
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] arr = super.getParameterValues(name);
        if (arr != null) {
            for (int i = 0; i < arr.length; i++) {
                arr[i] = JsoupUtil.clean(arr[i]);
            }
        }
        return arr;
    }


    /**
     * 覆盖getHeader方法，将参数名和参数值都做xss过滤。
     * 如果需要获得原始的值，则通过super.getHeaders(name)来获取
     * getHeaderNames 也可能需要覆盖
     *
     * @param name header name
     * @return clean
     */
    @Override
    public String getHeader(String name) {
        name = JsoupUtil.clean(name);
        String value = super.getHeader(name);
        if (McnUtils.isNotNullAndEmpty(value)) {
            value = JsoupUtil.clean(value);
        }
        return value;
    }

}
