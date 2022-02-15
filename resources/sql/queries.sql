-- :name save-message! :! :n
-- :doc creates a new message
INSERT INTO
  guestbook (name, message)
VALUES
  (:name, :message)

-- :name get-messages :? :*
-- :doc retrieves a user record given the id
SELECT
  *
FROM
  guestbook