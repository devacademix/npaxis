package com.digitalearn.npaxis.user;

import javax.annotation.processing.Generated;
import org.springframework.stereotype.Component;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2026-04-30T19:47:51+0530",
    comments = "version: 1.6.3, compiler: Eclipse JDT (IDE) 3.46.0.v20260407-0427, environment: Java 21.0.10 (Eclipse Adoptium)"
)
@Component
public class UserMapperImpl extends UserMapper {

    @Override
    public User toUserEntity(UserRequestDTO userRequestDto) {
        if ( userRequestDto == null ) {
            return null;
        }

        User.UserBuilder<?, ?> user = User.builder();

        user.email( userRequestDto.email() );
        user.password( userRequestDto.password() );

        return user.build();
    }

    @Override
    public UserResponseDTO toUserDTO(User user) {
        if ( user == null ) {
            return null;
        }

        Long userId = null;
        String displayName = null;
        String email = null;
        String role = null;

        userId = user.getUserId();
        displayName = user.getDisplayName();
        email = user.getEmail();
        role = roleToRoleString( user.getRole() );

        UserResponseDTO userResponseDTO = new UserResponseDTO( userId, displayName, email, role );

        return userResponseDTO;
    }

    @Override
    public LoggedInUserResponseDTO toLoggedInUserDTO(User user) {
        if ( user == null ) {
            return null;
        }

        LoggedInUserResponseDTO.LoggedInUserResponseDTOBuilder loggedInUserResponseDTO = LoggedInUserResponseDTO.builder();

        loggedInUserResponseDTO.email( user.getEmail() );
        loggedInUserResponseDTO.name( user.getName() );
        loggedInUserResponseDTO.userId( user.getUserId() );
        loggedInUserResponseDTO.username( user.getUsername() );

        return loggedInUserResponseDTO.build();
    }
}
