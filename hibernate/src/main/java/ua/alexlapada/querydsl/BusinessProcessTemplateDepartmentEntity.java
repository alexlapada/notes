package ua.alexlapada.querydsl;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "business_process_template_department")
@IdClass(BusinessProcessTemplateDepartmentEntity.CompositeId.class)
@Data
public class BusinessProcessTemplateDepartmentEntity {
  @Id
  @Column(name = "business_process_template_id")
  private String bpId;

  @Id
  @Column(name = "department_id")
  private String depId;

  @Data
  public static class CompositeId implements Serializable {
    private String bpId;
    private String depId;
  }
}
