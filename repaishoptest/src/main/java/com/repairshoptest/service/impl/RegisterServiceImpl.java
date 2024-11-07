package com.repairshoptest.service.impl;

import org.hibernate.usertype.UserType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.MethodArgumentNotValidException;

import com.repairshoptest.dto.ClerkRequestDTO;
import com.repairshoptest.dto.RegisterRequest;
import com.repairshoptest.dto.RegisterResponse;
import com.repairshoptest.dto.RepairPersonRequestDTO;
import com.repairshoptest.enums.UserRole;
import com.repairshoptest.model.User;
import com.repairshoptest.service.ClerkService;
import com.repairshoptest.service.RegisterService;
import com.repairshoptest.service.RepairPersonService;

@Service
public class RegisterServiceImpl implements RegisterService{

	@Autowired
	private ClerkService clerkService;
	
	@Autowired
	private RepairPersonService repairPersonService;
	
	@Override
	public RegisterResponse registerUser(RegisterRequest registerRequest){
		User user;
		if(registerRequest.getType().equals(UserRole.CLERK.getRole())) {
			ClerkRequestDTO clerkRequestDTO = registerRequest.getClerk();
			user = clerkService.add(clerkRequestDTO);
		}else {
			RepairPersonRequestDTO repairPersonRequestDTO = registerRequest.getRepairPerson();
			user = repairPersonService.add(repairPersonRequestDTO);
		}
		
		RegisterResponse registerResponse = new RegisterResponse("User Registered Successfully");
		return registerResponse;
	}

}
