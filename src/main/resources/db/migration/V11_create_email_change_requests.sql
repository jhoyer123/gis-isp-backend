CREATE TABLE email_change_requests (
                                              id UUID PRIMARY KEY,
                                              user_id uuid NOT NULL,
                                              new_email varchar(255) NOT NULL,
                                              token_hash varchar(255) NOT NULL UNIQUE,
                                              expires_at TIMESTAMPTZ NOT NULL,
                                              used bool DEFAULT false NOT NULL,
                                              created_at TIMESTAMPTZ DEFAULT now() NOT NULL,

                                              CONSTRAINT fk_email_change_requests_user FOREIGN KEY (user_id)
                                                  REFERENCES public.users(id) ON DELETE CASCADE
);

CREATE INDEX idx_email_change_requests_user_id
    ON public.email_change_requests USING btree (user_id);

CREATE INDEX idx_email_change_requests_expires_at
    ON public.email_change_requests USING btree (expires_at);