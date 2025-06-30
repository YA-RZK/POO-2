import pygame
import random

# Inicialização do Pygame
pygame.init()

# Configurações da janela
LARGURA, ALTURA = 1000, 600
TELA = pygame.display.set_mode((LARGURA, ALTURA))
pygame.display.set_caption("Batalha Automática RPG")

# Cores
VERDE = (34, 139, 34)
VERMELHO = (255, 0, 0)
AZUL = (0, 0, 255)
BRANCO = (255, 255, 255)
PRETO = (0, 0, 0)

# FPS
FPS = 60
clock = pygame.time.Clock()

# Fonte
font = pygame.font.SysFont("arial", 16)

# Classe base
class Personagem:
    def __init__(self, nome, x, y, vida, charge, forca, esquiva, time):
        self.nome = nome
        self.x = x
        self.y = y
        self.vida = vida
        self.charge = charge
        self.forca = forca
        self.esquiva = esquiva
        self.time = time
        self.vivo = True

    def atacar(self, alvo):
        if not self.vivo or not alvo.vivo:
            return
        if random.randint(1, 100) <= alvo.esquiva:
            return
        dano = self.forca + random.randint(0, self.charge)
        alvo.vida -= dano
        if alvo.vida <= 0:
            alvo.vida = 0
            alvo.vivo = False

    def desenhar_barra_vida(self):
        vida_percent = self.vida / 100
        barra_vida = max(0, int(30 * vida_percent))
        pygame.draw.rect(TELA, PRETO, (self.x - 15, self.y - 40, 30, 5))
        pygame.draw.rect(TELA, (0, 255, 0), (self.x - 15, self.y - 40, barra_vida, 5))


class Soldado(Personagem):
    def __init__(self, nome, x, y, time):
        super().__init__(nome, x, y, 100, 10, 15, 20, time)

    def desenhar(self):
        self.desenhar_barra_vida()
        cor = VERMELHO if self.time == "vermelho" else AZUL
        pygame.draw.circle(TELA, cor, (self.x, self.y), 15)


class Orc(Personagem):
    def __init__(self, nome, x, y, time):
        super().__init__(nome, x, y, 120, 5, 20, 10, time)

    def desenhar(self):
        self.desenhar_barra_vida()
        cor = VERMELHO if self.time == "vermelho" else AZUL
        pygame.draw.rect(TELA, cor, (self.x - 15, self.y - 15, 30, 30))


# Funções auxiliares
def gerar_posicao(time):
    if time == "vermelho":
        return random.randint(50, 200), random.randint(100, 500)
    else:
        return random.randint(800, 950), random.randint(100, 500)


def gerar_time_aleatorio(time):
    time_list = []
    total = random.randint(5, 10)
    for i in range(total):
        x, y = gerar_posicao(time)
        tipo = random.choice(["soldado", "orc"])
        if tipo == "soldado":
            time_list.append(Soldado(f"Soldado_{time}_{i+1}", x, y, time))
        else:
            time_list.append(Orc(f"Orc_{time}_{i+1}", x, y, time))
    return time_list


# Botões
def desenhar_botao(texto, x, y, largura, altura):
    pygame.draw.rect(TELA, PRETO, (x, y, largura, altura))
    pygame.draw.rect(TELA, BRANCO, (x + 2, y + 2, largura - 4, altura - 4))
    texto_render = font.render(texto, True, PRETO)
    TELA.blit(texto_render, (x + 5, y + 5))
    return pygame.Rect(x, y, largura, altura)


# Inicialização
vitorias_vermelho = 0
vitorias_azul = 0
batalha_ativa = True
time_vermelho = gerar_time_aleatorio("vermelho")
time_azul = gerar_time_aleatorio("azul")

# Loop principal
rodando = True
while rodando:
    clock.tick(FPS)

    for evento in pygame.event.get():
        if evento.type == pygame.QUIT:
            rodando = False
        if evento.type == pygame.MOUSEBUTTONDOWN:
            mx, my = pygame.mouse.get_pos()
            if botao_parar.collidepoint((mx, my)):
                batalha_ativa = False

    if batalha_ativa:
        vivos_vermelho = [p for p in time_vermelho if p.vivo]
        vivos_azul = [p for p in time_azul if p.vivo]

        if vivos_vermelho and vivos_azul:
            atacante = random.choice(vivos_vermelho + vivos_azul)
            inimigos = vivos_azul if atacante.time == "vermelho" else vivos_vermelho
            alvo = random.choice(inimigos)
            atacante.atacar(alvo)
        else:
            vencedor = "vermelho" if vivos_vermelho else "azul"
            if vencedor == "vermelho":
                vitorias_vermelho += 1
            else:
                vitorias_azul += 1

            time_vermelho = gerar_time_aleatorio("vermelho")
            time_azul = gerar_time_aleatorio("azul")

    # Fundo
    TELA.fill(VERDE)

    # Desenhar personagens
    for p in time_vermelho + time_azul:
        if p.vivo:
            p.desenhar()

    # Botão parar
    botao_parar = desenhar_botao("🛑 Parar Luta", LARGURA // 2 - 60, 20, 120, 35)

    # Placar
    texto_v = font.render(f"Vitórias Vermelho: {vitorias_vermelho}", True, PRETO)
    texto_a = font.render(f"Vitórias Azul: {vitorias_azul}", True, PRETO)
    TELA.blit(texto_v, (20, 20))
    TELA.blit(texto_a, (20, 45))

    pygame.display.update()

pygame.quit()