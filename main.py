import flet as ft

def main (page: ft.Page): #Recibe como parametro la pagina de flet (Interfaz donde van poniendo los elemntos de la aplicacion)
    page.bgcolor = ft.Colors.BLUE_GREY_800  # <- en muchas versiones es ft.colors
    page.horizontal_alignment = ft.CrossAxisAlignment.CENTER
    page.title = "CHISMERIA"

    texto = ft.Text("Mi primera app con Flet")
    texto2 = ft.Text("Este es el primer intento")

    def cambiar_texto(e):
        texto2.value = "Creo que si salió xd"
        page.update()

    boton = ft.FilledButton(content=ft.Text("Cambiar texto"), on_click=cambiar_texto)

    page.add(texto, texto2, boton)
ft.app(target=main)# Se va ejecutar la función de la aplicación
#ft.app(target=main, view= ft.WEB_BROWSER)# Se va ejecutar la función de la aplicación
