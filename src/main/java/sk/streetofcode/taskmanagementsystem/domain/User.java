package sk.streetofcode.taskmanagementsystem.domain;

import lombok.Value;

@Value
public class User {
    long id;
    String name;
    String email;
}
