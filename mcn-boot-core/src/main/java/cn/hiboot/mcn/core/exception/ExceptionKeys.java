package cn.hiboot.mcn.core.exception;

/**
 *
 *  <p>3xxxxx:通用错误码定义</p>
 *  <p>5xxxxx:业务相关错误码定义</p>
 *  <p>7xxxxx:未知错误码</p>
 *  <p>8xxxxx:Http相关错误码定义</p>
 *  <p>9xxxxx:统一错误码及第三方服务错误码定义</p>
 *
 *  @author DingHao
 *  @since 2021/11/30 22:46
 */
public interface ExceptionKeys {

    int PARAM_PARSE_ERROR = 300001;
    int JSON_PARSE_ERROR = 300002;
    int INIT_FAILED = 300003;
    int USER_PWD_ERROR = 300004;
    int GET_MOBILE_CODE_ERROR = 300005;
    int VERIFY_MOBILE_CODE_ERROR = 300006;
    int UPLOAD_FILE_SIZE_ERROR = 300007;
    int PARAM_TYPE_ERROR = 300008;
    int SPECIAL_SYMBOL_ERROR = 300009;
    int INVOKE_MINIO_ERROR = 300010;

    int UNKNOWN_ERROR = 700001;

    int HTTP_ERROR_400 = 800400;
    int HTTP_ERROR_401 = 800401;
    int HTTP_ERROR_402 = 800402;
    int HTTP_ERROR_403 = 800403;
    int HTTP_ERROR_404 = 800404;
    int HTTP_ERROR_405 = 800405;
    int HTTP_ERROR_406 = 800406;
    int HTTP_ERROR_408 = 800408;
    int HTTP_ERROR_409 = 800409;
    int HTTP_ERROR_413 = 800413;
    int HTTP_ERROR_415 = 800415;
    int HTTP_ERROR_421 = 800421;
    int HTTP_ERROR_500 = 800500;
    int HTTP_ERROR_503 = 800503;
    int HTTP_ERROR_504 = 800504;


    int SERVICE_ERROR = 900000;
    int THIRD_PARTY_ERROR = 900001;
    int REMOTE_SERVICE_ERROR = 900002;
    int REMOTE_DATA_PARSE_ERROR = 900003;

    static int mappingCode(Integer statusCode){
        if(statusCode != null){
            switch (statusCode){
                case 400: return HTTP_ERROR_400;
                case 401: return HTTP_ERROR_401;
                case 402: return HTTP_ERROR_402;
                case 403: return HTTP_ERROR_403;
                case 404: return HTTP_ERROR_404;
                case 405: return HTTP_ERROR_405;
                case 406: return HTTP_ERROR_406;
                case 408: return HTTP_ERROR_408;
                case 409: return HTTP_ERROR_409;
                case 413: return HTTP_ERROR_413;
                case 415: return HTTP_ERROR_415;
                case 421: return HTTP_ERROR_421;
                case 500: return HTTP_ERROR_500;
                case 503: return HTTP_ERROR_503;
                case 504: return HTTP_ERROR_504;
            }
        }
        return ExceptionKeys.HTTP_ERROR_500;
    }

}
