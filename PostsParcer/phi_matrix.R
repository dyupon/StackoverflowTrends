# Created by: User
# Created on: 4/9/2020

phi.matrix <- read.csv('phi_matrix_best_perplexity_model.csv', row.names = 1)
#entries <- sapply(1:dim(phi.matrix)[1], function(i) {length(which(phi.matrix[i, ] > 0))})
tags <- rownames(phi.matrix)
topics <- colnames(phi.matrix)

tag2topic <- sapply(seq_along(tags), function(i) {
  entries <- which(phi.matrix[i, ] > 0)
  result <- NULL
  for (entry in entries) {
    new.row <- c(tags[i], topics[entry], phi.matrix[i, entry])
    result <- rbind(result, new.row)
  }
  result
})

tag2topic <- do.call(rbind, tag2topic)
rownames(tag2topic) <- NULL
colnames(tag2topic) <- c("tag", "topic", "propensity")
write.csv(tag2topic, "tag2topic.csv", row.names = FALSE)
