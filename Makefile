NAME = Gomoku

SRCS = $(shell find src -name "*.java")
CLASSES = $(SRCS:.java=.class)

JAVAC = javac
JAVA = java

all: $(NAME)

$(NAME): $(SRCS)
	$(JAVAC) $(SRCS)
	@echo "#!/bin/sh" > $(NAME)
	@echo "$(JAVA) -cp src fr.game.board.BoardDisplay" >> $(NAME)
	@chmod +x $(NAME)

clean:
	find . -name "*.class" -type f -delete

fclean: clean
	rm -f $(NAME)

re: fclean all

.PHONY: all clean fclean re
