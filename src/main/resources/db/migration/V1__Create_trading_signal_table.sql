CREATE TABLE trading_signals (
    id BIGSERIAL PRIMARY KEY,

    symbol VARCHAR(20) NOT NULL,

    direction VARCHAR(10) NOT NULL,

    entry_price NUMERIC(28,8) NOT NULL,

    stop_loss NUMERIC(28,8) NOT NULL,

    target_price NUMERIC(28,8) NOT NULL,

    entry_time TIMESTAMPTZ NOT NULL,

    expiry_time TIMESTAMPTZ NOT NULL,

    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',

    realized_roi NUMERIC(10,2),

    CONSTRAINT chk_positive_prices
        CHECK (
            entry_price > 0
            AND stop_loss > 0
            AND target_price > 0
        )
);

CREATE INDEX idx_trading_signals_symbol
    ON trading_signals(symbol);

CREATE INDEX idx_trading_signals_status
    ON trading_signals(status);

CREATE INDEX idx_trading_signals_expiry_time
    ON trading_signals(expiry_time);