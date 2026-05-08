# Discord Clone Project

## Project Overview
This is a full-stack web application that replicates core functionality of Discord. The project uses a Java-based microservices architecture with a REST API backend and a web-based frontend, deployed on Apache TomEE and using MariaDB for persistence.

---

## Table of Contents
- [Architecture & Components](#architecture--components)  
    - [Backend (discord_clone_rest_service)](#backend-discord_clone_rest_service)  
    - [Frontend (discord_clone_client)](#frontend-discord_clone_client)  
- [Key Features Implemented](#key-features-implemented)  
- [Technology Stack](#technology-stack)  
- [Database Design](#database-design)  
- [Deployment & DevOps](#deployment--devops)  

---

## Architecture & Components

### Backend (discord_clone_rest_service)
REST API service built with Jakarta EE / JAX-RS.

Main resources and responsibilities:
- `App.java` — Core permissions endpoint
- `UserAuth.java` — User authentication
- `ServerResource.java` — Server management
- `Channel.java` — Channel operations
- `ServerChat.java` — Server messaging
- `DirectChats.java` — Direct messaging
- `Friends.java` — Friend management
- `Roles.java` — Role-based access control
- `Block.java` — User blocking functionality
- `CheckPermission.java` — Permission validation

### Frontend (discord_clone_client)
Jakarta Faces (JSF) web application with server-side rendering.

Managed beans and responsibilities:
- `AuthBean.java` — Authentication flow
- `ServerBean.java` — Server/group management
- `ServerChatBean.java` — Server messaging UI
- `DirectChatBean.java` — Direct message interface
- `FriendsBean.java` — Friend management UI

Other front-end notes:
- RESTful client integration for API consumption
- Responsive UI styled with Tailwind CSS

---

## Key Features Implemented
- ✅ Authentication & Authorization  
    - User registration and login  
    - Session management with Jakarta EE  
    - Role-based access control (RBAC) with granular permissions

- ✅ Servers & Channels  
    - Create/manage servers (public/private)  
    - Hierarchical channel structure within servers  
    - Channel-level role permission overrides  
    - Server invite system with optional user targeting

- ✅ Messaging System  
    - Server messages with file attachments  
    - Direct messages (one-to-one)  
    - Message attachments and file management  
    - Soft delete for messages (`is_deleted` flag)

- ✅ Role & Permission Management  
    - Predefined permissions: `INVITE_USER`, `KICK_USER`, `CREATE_CHANNEL`, `READ_CHANNEL`, `WRITE_CHANNEL`, `MANAGE`  
    - Server-wide and channel-specific role assignments  
    - Automatic "everyone" role assignment on server join  
    - Custom role creation by server owners

- ✅ Social Features  
    - Friend request system with acceptance tracking  
    - User blocking functionality  
    - Friend status management  
    - Search and discovery for public servers

- ✅ Database Triggers & Automation  
    - Automatic `general` channel creation on server setup  
    - Automatic `everyone` role assignment for new members  
    - Referential integrity via foreign keys

---

## Technology Stack

| Component | Technology |
|---|---|
| Backend Language | Java 11+ |
| Backend Framework | Jakarta EE (JAX-RS) |
| Frontend Language | Java (Managed Beans) |
| Frontend Framework | Jakarta Faces (JSF 4.0) |
| UI Styling | Tailwind CSS 3.x |
| Database | MariaDB 10.x |
| ORM / Data Access | JDBC with connection pooling |
| Application Server | Apache TomEE 10.1.4 (Plus) |
| Build Tool | Gradle 9.2.0 |
| JNDI | DataSource for connection pool management |
| Testing | JUnit 5 |

---

## Database Design
Comprehensive relational schema with 19+ tables, including:

- Core: `users`, `servers`, `channels`  
- Social: `friends`, `blocked_users`, `server_invites`  
- Messaging: `server_messages`, `direct_chats`, `direct_chat_messages` (with attachments)  
- Access Control: `roles`, `permissions`, `role_permissions`, `server_member_roles`, `channel_role_permissions`  
- Utilities: timestamps, soft deletes, file tracking

See `database.schema.md` for the full ERD.

---

## Deployment & DevOps
- Multi-module Gradle build for independent frontend/backend compilation  
- WAR artifacts deployed to TomEE  
- JNDI DataSource configuration for database connectivity  
- Development setup guide and troubleshooting: see `project_setup_guide_README.md`

---