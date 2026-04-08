# Database schema — Discord clone (descriptive draft)
## Tables

### users
- id (PK)
- user_uid
- display_name
- username (unique)
- email (unique)
- password
- created_at
- status

### friends
- id (PK)
- user_id (FK → users.id)
- friend_user_id (FK → users.id)
- accepted (boolean)
- created_at

### blocked_users
- id (PK)
- user_id (FK → users.id)
- blocked_user_id (FK → users.id)
- created_at

### servers
- id (PK)
- owner_id (FK → users.id)
- name
- description
- type (enum: private/public)
- invite_code
- created_at

### server_members
- id (PK)
- server_id (FK → servers.id)
- user_id (FK → users.id)
- joined_at

### roles
- id (PK)
- server_id (FK → servers.id)
- name
- created_by (FK → users.id)
- created_at

### server_member_roles
- id (PK)
- server_member_id (FK → server_members.id)
- role_id (FK → roles.id)

### role_permissions
- id (PK)
- role_id (FK → roles.id)
- permission_name

### channels
- id (PK)
- server_id (FK → servers.id)
- name
- created_by (FK → users.id)
- created_at

### channel_role_permissions
- id (PK)
- channel_id (FK → channels.id)
- role_id (FK → roles.id)
- can_read (boolean)
- can_write (boolean)

### server_messages
- id (PK)
- channel_id (FK → channels.id)
- user_id (FK → users.id)
- content (text)
- attachment_id (nullable, FK → server_message_attachment.id)
- created_at
- is_deleted (boolean)

### server_message_attachment
- id (PK)
- message_id (FK → server_messages.id)
- file_name
- file_path
- uploaded_at

### server_invites
- id (PK)
- server_id (FK → servers.id)
- invited_user_id (FK → users.id, nullable)
- invited_by_user_id (FK → users.id)
- accepted (boolean)
- created_at

### direct_chats
- id (PK)
- user_a_id (FK → users.id)
- user_b_id (FK → users.id)
- created_at

### direct_chat_messages
- id (PK)
- conversation_id (FK → direct_chats.id)
- sender_user_id (FK → users.id)
- content (text)
- attachment_id (nullable, FK → direct_chat_msg_attachment.id)
- created_at
- is_deleted (boolean)

### direct_chat_msg_attachment
- id (PK)
- direct_chat_msg_id (FK → direct_chat_messages.id)
- file_name
- file_path
- uploaded_at

## ERD (high-level relationships)
- Users → Servers (owner)
- Users → ServerMembers → Servers
- Servers → Channels → ServerMessages
- Servers → Roles → RolePermissions
- Channels ↔ RolePermissions (channel-level overrides)
- Users → DirectChats → DirectChatMessages

Notes:
- Use indexes on FK columns and unique constraints for username/email.
- Timestamps (created_at, uploaded_at) should use timezone-aware types if supported.
- Boolean flags (accepted, is_deleted, can_read, can_write) can be tinyint(1) in MySQL.
- Adjust nullable FK/attachment handling according to application needs.