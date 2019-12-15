library(ggplot2)
tags <- read.csv("tags.csv")
head(tags)

ggplot(data = tags, aes(x = count)) + geom_histogram(binwidth = 1) + xlim(0, 1000)
tags <- tags[order(-tags$count),]
tags$number <- 1:dim(tags)[1]
ggplot(data = tags, aes(x = number, y = count)) + geom_line(size = 1) + ylim(0, 5000)
ggplot(data = tags, aes(x = number, y = log(count))) + geom_line(size = 1) 