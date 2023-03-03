package com.tuigroup.tuihomework.services;

import com.tuigroup.tuihomework.dto.BranchDto;
import com.tuigroup.tuihomework.dto.RepositoryDto;
import com.tuigroup.tuihomework.client.model.Branch;
import com.tuigroup.tuihomework.client.model.Repository;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@NoArgsConstructor
public class RepositoryMapper {
    public RepositoryDto toRepositoryDto(Repository repository, List<Branch> branches) {
        return new RepositoryDto(
                repository.getName(),
                repository.getOwner().getLogin(),
                branches.stream().map(this::toBranchDto).toList()
        );
    }

    private BranchDto toBranchDto(Branch branch) {
        return new BranchDto(branch.getName(), branch.getCommit().getSha());
    }
}
