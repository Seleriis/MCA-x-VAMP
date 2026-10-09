# Atualização — MCA × EV e MCA × Vamp

| Minecraft | Plataforma / Java | Pacote MCA × Vamp local | Alvo das fontes MCA × Vamp |
|---|---|---:|---|
| 1.20.1 | Forge / Java 17 | 1.3.0 (MCA 7.7.1-beta.3; Vampirism 1.10.17) | 1.3.0 (testado localmente; não publicado) |
| 1.21.1 | NeoForge / Java 21 | 2.3.0 (MCA 7.7.36-beta.3; Vampirism 1.10.13) | 2.3.0 (testado localmente; não publicado) |

Os pacotes 1.3.0 e 2.3.0 foram compilados e testados localmente; ainda não foram publicados. Na 1.20.1, MCA 7.7.1 é incompatível com o JAR antigo 1.2.3; faça backup do mundo antes de trocar a versão do MCA.

1. Encerre o jogo e o servidor. Guarde uma cópia do mundo e das configurações antes da atualização.
2. Substitua o JAR anterior de cada addon pela versão correspondente ao Minecraft e ao loader utilizados. Mantenha apenas uma versão de cada addon na pasta `mods`.
3. Use as mesmas versões dos addons no cliente e no servidor. O protocolo do Vamp passou para 4; o EV também verifica a versão correspondente de seu canal.
4. Mantenha MCA, Easy Villagers, Vampirism e os mods opcionais nas combinações registradas no relatório de compatibilidade.
5. Inicie o servidor e confira os aldeões e processos existentes. A migração dos pedidos ocorre ao carregar os dados do mundo.

Não é necessário recapturar os moradores ou recriar os pedidos. IDs de mods, itens, snapshots e chaves anteriores continuam sendo lidos. A versão 1.20.1 do EV conserva o formato de salvamento da 1.0.2, acrescentando somente os dados necessários aos novos indicadores e à validação de processos.

## Missões e diário

Abra o Livro de Artes Ocultas e selecione **Missões**. O guia continua disponível. O diário mostra os pedidos aceitos pelo jogador que segura o livro, com páginas, objetivo, solicitante, localização, dimensão, recompensa e prazo. Emprestar o livro não transfere pedidos. Aceitação, entrega e cancelamento continuam nas conversas.

O acompanhamento destaca um solicitante por vez. Um morador fora dos chunks carregados aparece indisponível e conserva o pedido. Morte confirmada, remoção do totem ou controle incompatível encerram o pedido; a limpeza posterior respeita o cooldown.

O prazo usa ticks do mundo. Em servidor ativo, continua correndo enquanto o jogador estiver desconectado. Reiniciar o servidor conserva os prazos registrados; deixá-lo desligado não avança o tempo do mundo.

Pedidos antigos aceitos recebem cinco corações registrados. Novos pedidos registram a recompensa configurada quando são criados: entregas simples usam 3 por padrão, especiais e de especialistas usam 5, ajuda urgente e suprimentos após ataques usam 8. Alterar a configuração não muda o acordo de um pedido já criado.

## Configuração e combate

`respectMcaRelationshipsInCombat` vem desativada. Quando ativada, protege cônjuges, pais, filhos, irmãos e jogadores considerados amigos pelo próprio MCA contra seleção automática de alvo e mordidas autônomas. Agressão confirmada permite defesa pelo período configurado da facção. As restrições de idade, cura e comandos continuam sendo aplicadas.

Essa opção não altera preços nem autorizações de comércio. As recompensas por tipo ficam na seção `village_missions.rewards` da configuração comum do Vamp.

## Independência entre addons

O EV funciona sem o Vamp. O Vamp funciona sem o EV. O Vamp novo também conserva os caminhos legados testados: EV 1.0.2 no Forge e EV 2.1.0, 2.1.1 e 2.1.2 no NeoForge. Informações adicionais de facção e cura nos tooltips usam a API nova quando ela está disponível.

## Fontes e recompilação

Os arquivos `*-sources.jar` contêm as fontes. Os arquivos `*-project.zip` acrescentam scripts Gradle, wrapper, recursos e testes. JARs de MCA, EV, Vampirism, Werewolves e Bloodlines não são redistribuídos; coloque as dependências descritas em `gradle.properties` na pasta `local-mods` do respectivo projeto.

No PowerShell, selecione o JDK apropriado em `JAVA_HOME` e execute `./gradlew.bat build`. Para os testes em servidor, use `./gradlew.bat runGameTestServer`. `--offline` funciona quando as dependências já estiverem no cache. No EV Forge, `-Plocal_mods_dir=<pasta>` permite apontar a pasta de dependências.

O MCA beta Forge contém seletores de mixin já convertidos para SRG sem refmap. A matriz em ForgeGradle utilizou uma cópia de desenvolvimento com esses nomes traduzidos para os mappings oficiais. O script e os registros de preparação ficam nos materiais de validação; a dependência original de produção permanece intacta.

Esta entrega é local, para revisão. Nenhum artefato foi publicado.

## Estado da validação nesta entrega

Os quatro JARs foram compilados. Foram aprovados 223 testes unitários, com outros 10 desativados, e 46 GameTests nas execuções finais de EV Forge, EV NeoForge e Vamp NeoForge. No Vamp Forge, build e JUnit terminaram antes de uma falha posterior do cliente gráfico; falta repetir sua suíte final de servidor. A matriz completa tem registros anteriores e não foi repetida integralmente nos quatro binários definitivos.

Não há aprovação visual: nenhuma captura foi produzida. A tentativa Forge falhou ao criar a janela; a revisão automática de aprovação rejeitou a repetição por limite de uso. A inspeção de diário, indicadores, nomes longos, escalas e idiomas permanece pendente, assim como a execução visual NeoForge. Consulte o relatório entregue antes de considerar todos os critérios aprovados.
