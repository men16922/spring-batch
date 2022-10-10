package io.springbatch.springbatch;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@NoArgsConstructor
@Entity
public class Customer2 {

    @Id
    private Long id;
    private String firstName;
    private String lastName;
    private String birthdate;
}