# Validação — MCA × Vamp 2.3.1 (atualizado em 28/10/2026)

## Combinação utilizada

MCA 7.7.36-beta.3+1.21.1; Vampirism 1.10.13; Easy Villagers 1.21.1-1.1.42; MCA × EV 2.2.7; Werewolves 2.0.3.3; Bloodlines 3.0.9; NeoForge 21.1.234; Java 21.

## Resultados

- Build e compilação dos GameTests concluídos em 27/10/2026.
- JUnit em 27/10/2026: 97 encontrados e aprovados, 0 falhas, erros ou testes ignorados. Os três testes de contrato do MCA × EV passaram.
- GameTests: 32 obrigatórios aprovados no perfil all em 28/10/2026.
- Inicialização de produção em 28/10/2026 com o JAR entregue: aprovada, com mensagem Done.
- Criação de morador MCA, inspect, resync e save-all flush: aprovados no servidor de produção.
- Encerramento normal por stop: código 0.

As execuções de 28/10 utilizaram os JARs de distribuição, sem alteração de código nesta rodada.

## Evidências e limites

Evidências em 1.21.1/testing/2.3.1/2026-10-28/. Resultados anteriores de JUnit em testing/2.3.1/build/test-results/test; hashes de dependências em testing/2.3.1/dependencies.json. O [relatório conjunto](RELATORIO_TESTES_2026-10-28.md) acompanha o código e o pré-lançamento. Os logs completos foram preservados no workspace local.

Não houve erro de nível ERROR/FATAL ou erro de ligação pesquisado nos logs de runtime revisados. Há avisos das bibliotecas e dos mods opcionais, registrados no relatório. Essa validação cobre as combinações acima; não repete toda a matriz histórica.

Aparência, interface e FPS em cliente gráfico continuam na lista TESTES_MANUAIS.md. Nenhum mundo pessoal foi usado.
