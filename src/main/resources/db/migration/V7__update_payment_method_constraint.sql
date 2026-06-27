ALTER TABLE sales DROP CONSTRAINT sales_payment_method_check;
ALTER TABLE sales ADD CONSTRAINT sales_payment_method_check
    CHECK (payment_method IN ('EFECTIVO', 'YAPE'));
