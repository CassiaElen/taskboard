package com.boardmanager.service;

import com.boardmanager.entity.*;
import com.boardmanager.repository.BoardRepository;
import com.boardmanager.repository.CardRepository;
import com.boardmanager.repository.ColumnRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private ColumnRepository columnRepository;

    @Autowired
    private CardRepository cardRepository;

    private final Scanner scanner = new Scanner(System.in);

    public void iniciarMenu() {
        while (true) {
            System.out.println("\n==== MENU PRINCIPAL ====");
            System.out.println("1 - Criar novo board");
            System.out.println("2 - Selecionar board");
            System.out.println("3 - Excluir boards");
            System.out.println("4 - Sair");
            System.out.print("Escolha: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> criarNovoBoard();
                case 2 -> selecionarBoard();
                case 3 -> excluirBoards();
                case 4 -> {
                    System.out.println("Encerrando...");
                    return;
                }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void criarNovoBoard() {
        System.out.print("Nome do novo board: ");
        String nome = scanner.nextLine();
        Board board = new Board();
        board.setName(nome);
        boardRepository.save(board);

        System.out.println("Criando colunas padrão:");
        criarColuna(board, "Backlog", 0, ColumnType.INICIAL);
        criarColuna(board, "Em andamento", 1, ColumnType.PENDENTE);
        criarColuna(board, "Concluído", 2, ColumnType.FINAL);
        criarColuna(board, "Cancelado", 3, ColumnType.CANCELAMENTO);

        System.out.println("Board criado com sucesso.");
    }

    private void criarColuna(Board board, String nome, int ordem, ColumnType tipo) {
        Column coluna = new Column();
        coluna.setBoard(board);
        coluna.setName(nome);
        coluna.setOrderIndex(ordem);
        coluna.setType(tipo);
        columnRepository.save(coluna);
    }

    private void selecionarBoard() {
        List<Board> boards = boardRepository.findAll();
        if (boards.isEmpty()) {
            System.out.println("Nenhum board encontrado.");
            return;
        }

        System.out.println("Selecione um board:");
        for (int i = 0; i < boards.size(); i++) {
            System.out.printf("%d - %s\n", i + 1, boards.get(i).getName());
        }

        int escolha = scanner.nextInt() - 1;
        scanner.nextLine();

        if (escolha >= 0 && escolha < boards.size()) {
            manipularBoard(boards.get(escolha));
        } else {
            System.out.println("Opção inválida.");
        }
    }

    private void excluirBoards() {
        boardRepository.deleteAll();
        System.out.println("Todos os boards foram excluídos.");
    }

    private void manipularBoard(Board board) {
        while (true) {
            System.out.println("\n==== MENU DO BOARD: " + board.getName() + " ====");
            System.out.println("1 - Criar novo card");
            System.out.println("2 - Mover card para próxima coluna");
            System.out.println("3 - Cancelar card");
            System.out.println("4 - Bloquear card");
            System.out.println("5 - Desbloquear card");
            System.out.println("6 - Voltar");
            System.out.print("Escolha: ");
            int opcao = scanner.nextInt();
            scanner.nextLine();

            switch (opcao) {
                case 1 -> criarCard(board);
                case 2 -> moverCard(board);
                case 3 -> cancelarCard(board);
                case 4 -> bloquearCard(board);
                case 5 -> desbloquearCard(board);
                case 6 -> { return; }
                default -> System.out.println("Opção inválida.");
            }
        }
    }

    private void criarCard(Board board) {
        System.out.print("Título do card: ");
        String titulo = scanner.nextLine();
        System.out.print("Descrição: ");
        String descricao = scanner.nextLine();

        Column colunaInicial = board.getColumns().stream()
                .filter(c -> c.getType() == ColumnType.INICIAL)
                .findFirst()
                .orElseThrow();

        Card card = new Card();
        card.setTitle(titulo);
        card.setDescription(descricao);
        card.setCreationDate(LocalDateTime.now());
        card.setColumn(colunaInicial);

        ColumnMovement movement = new ColumnMovement();
        movement.setColumnName(colunaInicial.getName());
        movement.setEnteredAt(LocalDateTime.now());
        card.getMovements().add(movement);

        cardRepository.save(card);

        System.out.println("Card criado na coluna " + colunaInicial.getName());
    }

    private void moverCard(Board board) {
        List<Card> cards = cardRepository.findByColumnBoard(board);
        List<Card> moveis = cards.stream()
                .filter(card -> !card.isBlocked() && card.getColumn().getType() != ColumnType.FINAL)
                .toList();

        if (moveis.isEmpty()) {
            System.out.println("Nenhum card disponível para mover.");
            return;
        }

        System.out.println("Cards disponíveis:");
        for (int i = 0; i < moveis.size(); i++) {
            System.out.printf("%d - %s (%s)\n", i + 1, moveis.get(i).getTitle(), moveis.get(i).getColumn().getName());
        }

        int escolha = scanner.nextInt() - 1;
        scanner.nextLine();

        if (escolha < 0 || escolha >= moveis.size()) {
            System.out.println("Opção inválida.");
            return;
        }

        Card card = moveis.get(escolha);
        Column atual = card.getColumn();
        List<Column> ordenadas = board.getColumns();
        int idx = ordenadas.indexOf(atual);

        if (idx + 1 >= ordenadas.size() - 1) {
            System.out.println("Card já está na última coluna válida.");
            return;
        }

        Column proxima = ordenadas.get(idx + 1);
        card.getMovements().get(card.getMovements().size() - 1).setExitedAt(LocalDateTime.now());

        ColumnMovement movement = new ColumnMovement();
        movement.setColumnName(proxima.getName());
        movement.setEnteredAt(LocalDateTime.now());
        card.getMovements().add(movement);

        card.setColumn(proxima);
        cardRepository.save(card);
        System.out.println("Card movido para: " + proxima.getName());
    }

    private void cancelarCard(Board board) {
        // Lógica parecida com moverCard, só que direto para coluna CANCELAMENTO
    }

    private void bloquearCard(Board board) {
        // Listar cards não bloqueados, solicitar motivo, marcar bloqueado
    }

    private void desbloquearCard(Board board) {
        // Listar cards bloqueados, solicitar motivo, marcar desbloqueado
    }
}
