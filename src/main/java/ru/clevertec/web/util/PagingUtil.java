package ru.clevertec.web.util;

import jakarta.servlet.http.HttpServletRequest;
import lombok.Getter;

public class PagingUtil {

    private static final String PAGE = "page";
    private static final String SIZE = "size";

    public static Paging getPaging(HttpServletRequest req) {
        String sizeStr = req.getParameter(SIZE);
        int limit;
        if (sizeStr == null) {
            limit = 10;
        } else {
            limit = Integer.parseInt(sizeStr);
        }
        if (limit > 20) {
            limit = 20;
        }
        String pageStr = req.getParameter(PAGE);
        long page;
        if (pageStr == null) {
            page = 1;
        } else {
            page = Long.parseLong(pageStr);
        }
        long offset = (page - 1) * limit;
        return new Paging(limit, offset);
    }

    @Getter
    public static class Paging {
        private final int limit;
        private final long offset;

        public Paging(int limit, long offset) {
            this.limit = limit;
            this.offset = offset;
        }
    }
}
