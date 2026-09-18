# MCA × Vamp 2.2.3 — 1.21.1 NeoForge (RC)

Esta é uma pré-release para validação em jogo. Os arquivos abaixo são os mesmos JARs auditados nas entregas locais; não representam uma nova recompilação ou alteração de comportamento.

## Novidades

- Livro de Artes Ocultas com 42 páginas revisadas em pt_BR, pt_PT e en_US, preservando o visual original e dois marcadores laterais: guia em cima, missões embaixo.
- Diário com pedidos aceitos, paginação, acompanhamento, recompensa e tempo restante. Falas próprias para os 12 tipos de favor, com linguagem mais natural.
- Suprimentos após ataques e pedidos de especialistas; recompensas configuráveis de 3, 5 e 8 corações, registradas ao criar o pedido. Pedidos antigos mantêm cinco corações.
- Missões preservadas quando o morador está descarregado, com validação da entrega no servidor e limpeza de registros encerrados após o cooldown.
- Proteção opcional de família e amizade contra agressão automática, desligada por padrão; permite defesa após agressão.
- Integração opcional com os novos tooltips do MCA × EV, mantendo os caminhos legados. Correção da exibição de minutos/dias nas conversas.

Favores exigem um morador elegível numa vila com totem ativo e controle de facção. Um vampiro numa vila comum não basta. O prazo começa na aceitação e continua enquanto o mundo estiver rodando.

## Instalação

Use Minecraft 1.21.1, NeoForge e Java 21. Substitua o JAR antigo deste addon no cliente e no servidor, mantendo somente uma versão instalada. Não é necessário apagar configurações ou dados de aldeões/missões.

| Minecraft | MCA × Vamp | MCA × EV |
|---|---|---|
| 1.20.1 / Forge | 1.2.3 | 1.1.2 |
| 1.21.1 / NeoForge | 2.2.3 | 2.2.2 |

As dependências continuam separadas: MCA Reborn e Vampirism são obrigatórios; Werewolves, Bloodlines e Easy Villagers são integrações opcionais nas linhas já suportadas. Confira as versões e os requisitos desta branch em `gradle.properties` e no README. Não misture arquivos Forge e NeoForge.

## Validação e limites

O build desta versão passou, com 93 testes JUnit aprovados, 1 ignorados e nenhuma falha ou erro. JAR auditado quanto a versão, Java alvo, recursos/fontes e ausência do capturador de testes.
O cliente concluiu 177 capturas nesta linha: todas as páginas do guia nos três idiomas e cenários do diário nas escalas 1, 2 e 4. Nenhum texto de página ultrapassou sua área. As missões da captura usam dados fictícios; não equivalem a conversar com um NPC em um mundo conectado.

Antes de classificar como estável, falta a rodada final de missões em mundo real, persistência após reinício/descarregamento e servidor dedicado com os JARs finais e as combinações anunciadas. A matriz anterior e os GameTests históricos não foram integralmente repetidos nesta revisão.

Os arquivos `-sources.jar` e `-project.zip` contêm fontes; instale somente o JAR principal na pasta `mods`. Dependências de terceiros e mundos de teste não são redistribuídos. `SHA256SUMS.txt` permite conferir os downloads.
