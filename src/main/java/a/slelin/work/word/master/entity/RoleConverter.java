package a.slelin.work.word.master.entity;

import jakarta.persistence.Converter;

@Converter
public class RoleConverter extends StandardEnumConverter<Role> {

    public RoleConverter() {
        super(Role.class);
    }
}
