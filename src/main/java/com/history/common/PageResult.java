package com.history.common;

import com.github.pagehelper.PageInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

/**
 * 通用分页结果封装。
 *
 * @author Diamond
 * @param <T> 数据类型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageResult<T> implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 当前页码。
     */
    private int pageNum;

    /**
     * 每页数量。
     */
    private int pageSize;

    /**
     * 总记录数。
     */
    private long total;

    /**
     * 总页数。
     */
    private int pages;

    /**
     * 当前页数据列表。
     */
    private List<T> list;

    /**
     * 是否有下一页。
     */
    private boolean hasNext;

    /**
     * 是否有上一页。
     */
    private boolean hasPrevious;

    /**
     * 从 PageHelper 的 PageInfo 构造。
     *
     * @param pageInfo PageHelper 的 PageInfo 对象
     */
    public PageResult(PageInfo<T> pageInfo) {
        this.pageNum = pageInfo.getPageNum();
        this.pageSize = pageInfo.getPageSize();
        this.total = pageInfo.getTotal();
        this.pages = pageInfo.getPages();
        this.list = pageInfo.getList();
        this.hasNext = pageInfo.isHasNextPage();
        this.hasPrevious = pageInfo.isHasPreviousPage();
    }

    /**
     * 手动构造分页结果。
     *
     * @param pageNum  当前页码
     * @param pageSize 每页数量
     * @param total    总记录数
     * @param list     当前页数据
     * @return 分页结果
     */
    public static <T> PageResult<T> of(int pageNum, int pageSize, long total, List<T> list) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages((int) Math.ceil((double) total / pageSize));
        result.setList(list);
        result.setHasNext(pageNum < result.getPages());
        result.setHasPrevious(pageNum > 1);
        return result;
    }
}
