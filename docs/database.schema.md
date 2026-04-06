Tables (simple descriptions)

users

id (PK)
useruid (UUID string)
displayname
username (unique)
email (unique)
password (hashed)
created_at
status (online/offline/idle)
friends

id (PK)
userid (FK -> users.id)
friend_userid (FK -> users.id)
accepted (boolean)
created_at
Notes: one row per friendship/request; unique pair constraint to avoid duplicates.
blocked_users

id (PK)
userid (FK -> users.id)
blocked_userid (FK -> users.id)
created_at
servers

id (PK)
owner_id (FK -> users.id)
name
description
type (private/public)
invite_code (optional)
created_at
server_members

id (PK)
server_id (FK -> servers.id)
user_id (FK -> users.id)
joined_at
Notes: unique (server_id, user_id)
roles

id (PK)
server_id (FK -> servers.id)
name
created_by (FK -> users.id)
created_at
server_member_roles

id (PK)
server_member_id (FK -> server_members.id)
role_id (FK -> roles.id)
role_permissions

id (PK)
role_id (FK -> roles.id)
permission_name (INVITE_USER, KICK_MEMBER, CREATE_CHANNEL, READ_CHANNEL, WRITE_CHANNEL, MANAGE_ROLES)
channels

id (PK)
server_id (FK -> servers.id)
name
created_by (FK -> users.id)
created_at
channel_role_permissions

id (PK)
channel_id (FK -> channels.id)
role_id (FK -> roles.id)
can_read (boolean)
can_write (boolean)
server_messages

id (PK)
channel_id (FK -> channels.id)
userid (FK -> users.id)
content (text)
attachment_id (FK -> server_message_attachment.id, optional)
created_at
is_deleted (boolean)
server_message_attachment

id (PK)
message_id (FK -> server_messages.id)
file_name
file_path (or URL)
uploaded_at
server_invites

id (PK)
server_id (FK -> servers.id)
invited_userid (FK -> users.id, optional)
invitedby_userid (FK -> users.id, optional)
accepted (boolean)
created_at
direct_chats

id (PK)
user_a (FK -> users.id)
user_b (FK -> users.id)
created_at
Notes: store smaller user id first to enforce uniqueness
direct_chat_messages

id (PK)
conversation_id (FK -> direct_chats.id)
sender_userid (FK -> users.id)
content (text)
attachment_id (FK -> direct_chat_msg_attachment.id, optional)
created_at
is_deleted (boolean)
direct_chat_msg_attachment

id (PK)
direct_chat_msg_id (FK -> direct_chat_messages.id)
file_name
file_path (or URL)
uploaded_at