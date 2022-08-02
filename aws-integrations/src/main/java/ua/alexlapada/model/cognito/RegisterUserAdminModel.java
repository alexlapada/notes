package ua.alexlapada.model.cognito;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterUserAdminModel {

  private Integer invitationId;
  private String companyName;
  private String email;
  private String password;
  private String firstName;
  private String lastName;
  private String address1;
  private String address2;
  private String zipcode;
  private String city;
  private String state;
  private String phone1;
  private String phone2;
  private boolean publicSp;
}
