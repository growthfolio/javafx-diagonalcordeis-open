package com.diagonal.cordeis.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String password;
    
    private String nome;
    
    @ElementCollection(targetClass = Role.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>();
    
    @ElementCollection(targetClass = Permission.class, fetch = FetchType.EAGER)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Permission> permissions = new HashSet<>();
    
    private boolean ativo = true;

    @Enumerated(EnumType.STRING)
    private UserProfile profile = UserProfile.USER;

    @Column(name = "last_login")
    private java.time.LocalDateTime lastLogin;

    @Column(name = "session_id")
    private String sessionId;

    @Column(name = "login_attempts")
    private Integer loginAttempts = 0;

    public boolean isActive() {
        return ativo;
    }

    public void setActive(boolean active) {
        this.ativo = active;
    }

    public void addRole(Role role) {
        if (roles == null) {
            roles = new HashSet<>();
        }
        roles.add(role);
    }

    public boolean hasRole(Role role) {
        return roles != null && roles.contains(role);
    }

    public void addPermission(Permission permission) {
        if (permissions == null) {
            permissions = new HashSet<>();
        }
        permissions.add(permission);
    }

    public boolean hasPermission(Permission permission) {
        return permissions != null && permissions.contains(permission);
    }

    public void removePermission(Permission permission) {
        if (permissions != null) {
            permissions.remove(permission);
        }
    }

    public Set<Permission> getDefaultPermissions() {
        Set<Permission> defaultPermissions = new HashSet<>();
        
        if (hasRole(Role.ROLE_ADMIN) || profile == UserProfile.ADMIN) {
            // Admins get all permissions
            for (Permission permission : Permission.values()) {
                defaultPermissions.add(permission);
            }
        } else if (hasRole(Role.ROLE_USER) || profile == UserProfile.USER) {
            // Regular users get view and print permissions only
            defaultPermissions.add(Permission.VIEW_BOOKS);
            defaultPermissions.add(Permission.VIEW_COLLECTIONS);
            defaultPermissions.add(Permission.VIEW_BOOK_DETAILS);
            defaultPermissions.add(Permission.PRINT_BOOKS);
        } else if (profile == UserProfile.PUBLIC) {
            // Public users only get access to view books and print them
            defaultPermissions.add(Permission.VIEW_BOOKS);
            defaultPermissions.add(Permission.PRINT_BOOKS);
            defaultPermissions.add(Permission.VIEW_PRINTERS);
        }
        
        return defaultPermissions;
    }
}
