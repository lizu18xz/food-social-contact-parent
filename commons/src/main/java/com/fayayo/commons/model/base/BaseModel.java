package com.fayayo.commons.model.base;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc
 */
@Getter
@Setter
public class BaseModel implements Serializable {

    private Integer id;

    private Date createDate;

    private Date updateDate;

    private int isValid;

}
