CREATE TABLE IF NOT EXISTS guilds (
  id               INTEGER AUTO_INCREMENT NOT NULL,
  name             VARCHAR(64) NOT NULL,
  tag              VARCHAR(5) NOT NULL,
  description      VARCHAR(1000),
  leader_player_id INTEGER NOT NULL,
  level            INTEGER NOT NULL DEFAULT 1,
  xp               BIGINT NOT NULL DEFAULT 0,
  created_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uq_guilds_name UNIQUE (name),
  CONSTRAINT uq_guilds_tag UNIQUE (tag),
  CONSTRAINT chk_guilds_tag_len CHECK (CHAR_LENGTH(tag) BETWEEN 4 AND 5)
);

CREATE INDEX IF NOT EXISTS i_guilds_name ON guilds(name);
CREATE INDEX IF NOT EXISTS i_guilds_tag ON guilds(tag);
CREATE INDEX IF NOT EXISTS i_guilds_leader_player_id ON guilds(leader_player_id);

CREATE TABLE IF NOT EXISTS guild_members (
  guild_id   INTEGER NOT NULL,
  player_id  INTEGER NOT NULL,
  role       VARCHAR(16) NOT NULL DEFAULT 'MEMBER',
  joined_at  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (guild_id, player_id),
  CONSTRAINT chk_guild_members_role CHECK (role IN ('LEADER', 'OFFICER', 'MEMBER')),
  CONSTRAINT fk_guild_members_guild
    FOREIGN KEY (guild_id)
    REFERENCES guilds(id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS i_guild_members_player_id ON guild_members(player_id);
CREATE INDEX IF NOT EXISTS i_guild_members_guild_id ON guild_members(guild_id);

CREATE TABLE IF NOT EXISTS guild_invites (
  id                   INTEGER AUTO_INCREMENT NOT NULL,
  guild_id             INTEGER NOT NULL,
  invited_player_id    INTEGER NOT NULL,
  invited_by_player_id INTEGER NOT NULL,
  expires_at           TIMESTAMP NOT NULL,
  created_at           TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT uq_guild_invites_pending UNIQUE (guild_id, invited_player_id),
  CONSTRAINT fk_guild_invites_guild
    FOREIGN KEY (guild_id)
    REFERENCES guilds(id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS i_guild_invites_guild_id ON guild_invites(guild_id);
CREATE INDEX IF NOT EXISTS i_guild_invites_invited_player_id ON guild_invites(invited_player_id);
CREATE INDEX IF NOT EXISTS i_guild_invites_invited_by_player_id ON guild_invites(invited_by_player_id);

CREATE TABLE IF NOT EXISTS guild_logs (
  id              INTEGER AUTO_INCREMENT NOT NULL,
  guild_id        INTEGER NOT NULL,
  actor_player_id INTEGER,
  event_type      VARCHAR(64) NOT NULL,
  payload_json    TEXT,
  created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_guild_logs_guild
    FOREIGN KEY (guild_id)
    REFERENCES guilds(id)
    ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS i_guild_logs_guild_id_created_at ON guild_logs(guild_id, created_at);
CREATE INDEX IF NOT EXISTS i_guild_logs_actor_player_id ON guild_logs(actor_player_id);