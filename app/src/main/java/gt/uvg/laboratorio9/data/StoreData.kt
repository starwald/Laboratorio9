package gt.uvg.laboratorio9.data

import gt.uvg.laboratorio9.model.Producer
import gt.uvg.laboratorio9.model.Product

val products = listOf(
    Product(
        id = 1,
        name = "Café Antigua",
        description = "Café guatemalteco con aroma intenso y notas de chocolate.",
        price = 85.0,
        producerId = 1
    ),

    Product(
        id = 2,
        name = "Café Huehuetenango",
        description = "Café de altura con notas frutales y un sabor suave.",
        price = 90.0,
        producerId = 2
    ),

    Product(
        id = 3,
        name = "Café Atitlán",
        description = "Café cultivado cerca del lago de Atitlán con sabor balanceado.",
        price = 80.0,
        producerId = 1
    )
)

val producers = listOf(
    Producer(
        id = 1,
        name = "Finca El Cafetal",
        role = "Productor de café",
        location = "Antigua Guatemala",
        description = "Finca dedicada al cultivo y producción de café guatemalteco de altura."
    ),

    Producer(
        id = 2,
        name = "Finca Los Pinos",
        role = "Productor de café",
        location = "Huehuetenango, Guatemala",
        description = "Productores especializados en café de altura con procesos artesanales."
    )
)