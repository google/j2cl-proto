package com.google.protobuf.contrib.j2cl.generator;


import com.google.auto.value.AutoValue;
import com.google.common.base.Ascii;
import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.OneofDescriptor;
import com.google.protobuf.contrib.JavaQualifiedNames;
import java.util.ArrayList;
import java.util.List;

/** Represents a protocol buffer union */
@AutoValue
public abstract class TemplateOneOfDescriptor extends AbstractTemplateTypeDescriptor {

  public static TemplateOneOfDescriptor create(OneofDescriptor oneofDescriptor) {
    return new AutoValue_TemplateOneOfDescriptor(oneofDescriptor);
  }

  @Override
  abstract OneofDescriptor descriptor();

  @Override
  Descriptor getContainingType() {
    return descriptor().getContainingType();
  }

  @Override
  public String getName() {
    return JavaQualifiedNames.underscoresToCamelCase(descriptor().getName(), true) + "Case";
  }

  public boolean isDense() {
    // TODO(b/143500098): support dense in oneofs.
    return false;
  }

  public boolean isJsEnum() {
    // TODO(b/143500098): support js enum in oneofs.
    return false;
  }

  public List<TemplateEnumValueDescriptor> getValues() {
    List<TemplateEnumValueDescriptor> values = new ArrayList<>();
    String defaultValueName =
        Ascii.toUpperCase(descriptor().getName().replace("_", "")) + "_NOT_SET";
    values.add(TemplateEnumValueDescriptor.create(defaultValueName, 0));
    descriptor().getFields().forEach(e -> values.add(createEnumValue(e)));
    return values;
  }

  private static TemplateEnumValueDescriptor createEnumValue(FieldDescriptor fd) {
    return TemplateEnumValueDescriptor.create(Ascii.toUpperCase(fd.getName()), fd.getNumber());
  }

  /** Represents a protocol buffer union's getter field */
  @AutoValue
  public abstract static class OneOfField {
    abstract TemplateOneOfDescriptor oneOf();

    public String getName() {
      return oneOf().getName();
    }

    public String getJsName() {
      // Doesn't have the risk of collision due to 'Case' suffix so we can simply return name.
      return getName();
    }

    // TODO(goktug): Rename to getType.
    public String getUnboxedType() {
      return oneOf().getName();
    }

    public boolean isRepeated() {
      return false;
    }

    public boolean isEnum() {
      return true;
    }
  }

  public OneOfField asField() {
    return new AutoValue_TemplateOneOfDescriptor_OneOfField(this);
  }
}
