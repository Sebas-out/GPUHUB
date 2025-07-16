   ALTER TABLE cambios MODIFY COLUMN id_trabajador bigint NULL;
   
      ALTER TABLE cambios DROP FOREIGN KEY ;

   ALTER TABLE cambios
   ADD CONSTRAINT 
   FOREIGN KEY (id_trabajador) REFERENCES trabajadores(id_trabajador)
   ON DELETE SET NULL;

      ALTER TABLE cambios MODIFY COLUMN id_almacen bigint NULL;

         ALTER TABLE cambios DROP FOREIGN KEY ;

   ALTER TABLE cambios
   ADD CONSTRAINT 
   FOREIGN KEY (id_almacen) REFERENCES almacen(id_almacen)
   ON DELETE SET NULL;

      ALTER TABLE cambios MODIFY COLUMN id_producto bigint NULL;

         ALTER TABLE cambios DROP FOREIGN KEY ;

   ALTER TABLE cambios
   ADD CONSTRAINT 
   FOREIGN KEY (id_producto) REFERENCES producto(id_producto)
   ON DELETE SET NULL;