package com.cyecize.summer.areas.validation.constraints;

import com.cyecize.summer.areas.routing.interfaces.UploadedFile;
import com.cyecize.summer.areas.validation.interfaces.ConstraintValidator;
import com.cyecize.summer.common.annotations.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class MaxLengthConstraint implements ConstraintValidator<MaxLength, Object> {

    private long maxLen;

    @Override
    public void initialize(MaxLength constraintAnnotation) {
        this.maxLen = constraintAnnotation.length();
    }

    @Override
    public boolean isValid(Object field, Object bindingModel) {
        if (field == null) return 0 <= this.maxLen;

        if (UploadedFile.class.isAssignableFrom(field.getClass())) {
            return ((UploadedFile) field).getUploadedFile().getFileLength() <= this.maxLen;
        }

        if (Collection.class.isAssignableFrom(field.getClass())) {
            return ((Collection) field).size() <= this.maxLen;
        }

        if (Map.class.isAssignableFrom(field.getClass())) {
            return ((Map) field).size() <= this.maxLen;
        }

        return (field + "").length() <= this.maxLen;
    }
}
