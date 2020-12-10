package com.test.entity;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;


@Entity
@Table(name="JOB")
@ToString
public class Job {
    @Id
    @Getter
    private Long id;

    @Getter
    @Setter
    private String url;

    @Enumerated(EnumType.STRING)
    @Getter
    @Setter
    private Status status;

    @Getter
    @Setter
    private Integer httpStatusCode;
}