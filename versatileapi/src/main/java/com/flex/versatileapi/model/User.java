package com.flex.versatileapi.model;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class User {
	String user_id;
	String password;
	String email;
	
	String new_password;
	String new_email;
}