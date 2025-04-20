CREATE TABLE time_entries (
    id SERIAL PRIMARY KEY, -- Changed from BIGINT AUTO_INCREMENT
    user_id BIGINT NOT NULL,
    task_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration INT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Removed ON UPDATE
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (task_id) REFERENCES tasks(id)
);

-- Add trigger for automatic updated_at
CREATE TRIGGER set_time_entries_timestamp
BEFORE UPDATE ON time_entries
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();