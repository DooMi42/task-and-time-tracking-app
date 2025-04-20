CREATE TABLE tasks (
    id SERIAL PRIMARY KEY, -- Changed from BIGINT AUTO_INCREMENT
    title VARCHAR(255) NOT NULL,
    description TEXT,
    status VARCHAR(20) DEFAULT 'TODO',
    priority VARCHAR(20) DEFAULT 'MEDIUM',
    due_date DATE,
    user_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, -- Removed ON UPDATE
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- Add trigger for automatic updated_at
CREATE TRIGGER set_tasks_timestamp
BEFORE UPDATE ON tasks
FOR EACH ROW
EXECUTE FUNCTION update_timestamp();