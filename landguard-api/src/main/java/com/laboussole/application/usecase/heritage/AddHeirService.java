package com.laboussole.application.usecase.heritage;

import com.laboussole.domain.model.Email;
import com.laboussole.domain.model.User;
import com.laboussole.domain.model.UserId;
import com.laboussole.domain.model.heritage.Heir;
import com.laboussole.domain.model.heritage.SuccessionPlan;
import com.laboussole.domain.port.in.heritage.AddHeirUseCase;
import com.laboussole.domain.port.out.SuccessionRepository;
import com.laboussole.domain.port.out.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AddHeirService implements AddHeirUseCase {

    private final SuccessionRepository repository;
    private final UserRepository users;

    public AddHeirService(SuccessionRepository repository, UserRepository users) {
        this.repository = repository;
        this.users = users;
    }

    @Override
    @Transactional
    public SuccessionPlan execute(Command command) {
        SuccessionPlan plan = repository.findById(command.planId())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Succession plan not found: " + command.planId()));

        plan.addHeir(Heir.create(
                command.fullName(),
                command.relation(),
                command.sharePercentage(),
                resolveLinkedAccount(command.accountEmail())));
        return repository.save(plan);
    }

    /**
     * Feature 04.1: an heir may be linked to an existing platform account by
     * e-mail. An unknown e-mail is rejected rather than silently ignored, so
     * the family is never left believing an unreachable member is covered.
     */
    private UserId resolveLinkedAccount(String accountEmail) {
        if (accountEmail == null || accountEmail.isBlank()) {
            return null;
        }
        return users.findByEmail(Email.of(accountEmail))
                .map(User::id)
                .orElseThrow(() -> new IllegalArgumentException(
                        "No account found for e-mail: " + accountEmail));
    }
}
