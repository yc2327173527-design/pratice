package com.yupi.yupicturebackend.model.dto.note;

import lombok.Data;

import java.io.Serializable;

/**
 * Create note request
 */
@Data
public class NoteAddRequest implements Serializable {

    /**
     * note content
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
