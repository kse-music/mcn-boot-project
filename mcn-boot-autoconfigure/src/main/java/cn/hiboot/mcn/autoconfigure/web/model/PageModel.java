package cn.hiboot.mcn.autoconfigure.web.model;

import cn.hiboot.mcn.core.model.BaseModel;
import io.swagger.annotations.ApiParam;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.QueryParam;
import java.util.Objects;

/**
 * 分页模型
 *
 * @author DingHao
 * @since 2021/6/30 15:45
 */
public class PageModel extends BaseModel {

    @ApiParam("当前页，默认1")
    @DefaultValue("1")
    @Min(1)
    @QueryParam("pageNo")
    private Integer pageNo;
    @ApiParam("每页数，默认10")
    @DefaultValue("10")
    @Max(50)
    @QueryParam("pageSize")
    private Integer pageSize;

    public PageModel(){}

    public PageModel(Integer pageNo, Integer pageSize) {
        this.pageNo = pageNo;
        this.pageSize = pageSize;
    }

    public Integer getPageNo() {
        if(Objects.isNull(pageNo)){//此处不能用三目表达式，猜测利用反射获取的值不能带逻辑判断？
            return pageNo;
        }
        return (pageNo - 1) * pageSize;
    }

    public void setPageNo(Integer pageNo) {
        this.pageNo = pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }
}
