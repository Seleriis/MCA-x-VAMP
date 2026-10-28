# Validação — MCA × Vamp 1.3.1 (atualizado em 28/10/2026)

## Combinação utilizada

MCA 7.7.1-beta.5+1.20.1; Vampirism 1.10.17; Easy Villagers 1.20.1-1.1.39; MCA × EV 1.1.9; Werewolves 2.0.2.7; Forge 47.4.20; Java 17.

## Resultados

- Build e compilação dos GameTests concluídos em 27/10/2026.
- JUnit em 27/10/2026: 95 encontrados: 85 aprovados, 10 ignorados, 0 falhas ou erros. Os três testes de contrato do MCA × EV passaram.
- GameTests: 37 obrigatórios aprovados no perfil all em 27/10/2026.
- Inicialização de produção em 28/10/2026 com o JAR entregue: aprovada, com mensagem Done.
- Criação de morador MCA, inspect, resync e save-all flush: aprovados no servidor de produção.
- Encerramento normal por stop: código 0.
- Contrato de mixins/refmap contra o Minecraft SRG: aprovado. As 334 entradas do JAR verificado têm conteúdo idêntico às do JAR entregue.

As execuções de 28/10 utilizaram os JARs de distribuição, sem alteração de código nesta rodada.

A tentativa anterior inconclusiva de inicialização foi superada por esse novo ensaio bem-sucedido. Sua causa não foi determinada; o log anterior foi preservado. O GameTest Forge usa uma cópia do MCA com seletores mapeados apenas para userdev; a inicialização normal usa o MCA oficial sem modificá-lo nem incluí-lo no addon.

## Evidências e limites

Evidências em 1.20.1/testing/1.3.1/2026-10-28/. Resultados anteriores de JUnit em testing/1.3.1/build/test-results/test; hashes de dependências em testing/1.3.1/dependencies.json. O [relatório conjunto](RELATORIO_TESTES_2026-10-28.md) acompanha o código e o pré-lançamento. Os logs completos foram preservados no workspace local.

Não houve erro de nível ERROR/FATAL ou erro de ligação pesquisado nos logs de runtime revisados. Há avisos das bibliotecas e dos mods opcionais, registrados no relatório. Essa validação cobre as combinações acima; não repete toda a matriz histórica.

Aparência, interface e FPS em cliente gráfico continuam na lista TESTES_MANUAIS.md. Nenhum mundo pessoal foi usado.
